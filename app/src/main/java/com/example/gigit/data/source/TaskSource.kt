package com.example.gigit.data.source

import com.example.gigit.data.model.Message
import com.example.gigit.data.model.Review
import com.example.gigit.data.model.Task
import com.example.gigit.util.Constants
import com.example.gigit.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class TaskSource(private val firestore: FirebaseFirestore) {

    suspend fun postNewTask(task: Task): Resource<Unit> {
        return try {
            firestore.collection(Constants.TASKS_COLLECTION).add(task).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred.")
        }
    }

    // This provides a real-time stream of open tasks
    fun getOpenTasks(currentUserId: String, category: String): Flow<Resource<List<Task>>> = callbackFlow {
        val twentyFourHoursAgo = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24))

        var query: Query = firestore.collection(Constants.TASKS_COLLECTION)
            .whereEqualTo("status", Constants.TASK_STATUS_OPEN)
            .whereGreaterThan("createdAt", twentyFourHoursAgo)
            .whereNotEqualTo("posterId", currentUserId)

        // Apply the category filter only if it's not "All"
        if (category != "All") {
            query = query.whereEqualTo("category", category)
        }

        val listenerRegistration = query.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage))
                    close(error)
                } else if (snapshot != null) {
                    trySend(Resource.Success(snapshot.toObjects()))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getTaskDetails(taskId: String): Resource<Task?> {
        return try {
            val document =
                firestore.collection(Constants.TASKS_COLLECTION).document(taskId).get().await()
            Resource.Success(document.toObject<Task>())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    suspend fun acceptTask(taskId: String, taskerId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.TASKS_COLLECTION).document(taskId).update(
                mapOf(
                    "status" to Constants.TASK_STATUS_RESERVED,
                    "taskerId" to taskerId,
                    "participantIds" to FieldValue.arrayUnion(taskerId),
                    "acceptedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }


    fun getMessages(taskId: String): Flow<Resource<List<Message>>> = callbackFlow {
        val messagesCollection = firestore.collection(Constants.TASK_CHATS_COLLECTION)
            .document(taskId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = messagesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Failed to listen for messages."))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Resource.Success(snapshot.toObjects()))
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(taskId: String, message: Message): Resource<Unit> {
        return try {
            firestore.collection(Constants.TASK_CHATS_COLLECTION)
                .document(taskId)
                .collection("messages")
                .add(message).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    fun getActiveGigs(userId: String): Flow<Resource<List<Task>>> = callbackFlow {

        val listener = firestore.collection(Constants.TASKS_COLLECTION)
            .whereEqualTo("status", Constants.TASK_STATUS_RESERVED) // Find only active tasks
            .whereArrayContains("participantIds", userId) // Where the user is a participant
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to listen for active gigs."))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(Resource.Success(snapshot.toObjects()))
                }
            }
        awaitClose { listener.remove() }
    }


    suspend fun markTaskAsCompleted(taskId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.TASKS_COLLECTION).document(taskId)
                .update(
                    "status", Constants.TASK_STATUS_COMPLETED,
                    "completedAt" to FieldValue.serverTimestamp()
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    suspend fun submitReview(review: com.example.gigit.data.model.Review): Resource<Unit> {
        return try {
            firestore.collection(Constants.REVIEWS_COLLECTION).add(review).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    fun getReviewsForUser(userId: String): Flow<Resource<List<Review>>> = callbackFlow {
        val listener = firestore.collection(Constants.REVIEWS_COLLECTION)
            .whereEqualTo("revieweeId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage))
                } else if (snapshot != null) {
                    trySend(Resource.Success(snapshot.toObjects()))
                }
            }
        awaitClose { listener.remove() }
    }
}
