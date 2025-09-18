package com.example.gigit.data.source

import com.example.gigit.data.model.AppNotification
import com.example.gigit.util.Constants
import com.example.gigit.util.Resource
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationSource(private val firestore: FirebaseFirestore) {

    fun getUserNotifications(userId: String): Flow<Resource<List<AppNotification>>> = callbackFlow {
        val listener = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30) // Get the 30 most recent notifications
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to get notifications."))
                } else if (snapshot != null) {
                    trySend(Resource.Success(snapshot.toObjects()))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotification(notification: AppNotification): Resource<Unit> {
        return try {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION).add(notification).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }
}
