package com.example.gigit.data.source

import com.example.gigit.data.model.User
import com.example.gigit.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class UserSource(private val firestore: FirebaseFirestore) {

    suspend fun createUserProfile(user: User) {
        firestore.collection(Constants.USERS_COLLECTION).document(user.uid).set(user).await()
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        firestore.collection(Constants.USERS_COLLECTION).document(userId)
            .update("fcmToken", token).await()
    }

    suspend fun getUserProfile(userId: String): User? {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
                .toObject<User>()
        } catch (e: Exception) {
            // Return null if the user doesn't exist or an error occurs
            null
        }
    }

    suspend fun updateUpiId(userId: String, upiId: String) {
        firestore.collection(Constants.USERS_COLLECTION).document(userId)
            .update("upiId", upiId).await()
    }
}
