package com.example.gigit.data.source

import com.example.gigit.data.model.User
import com.example.gigit.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserSource(private val firestore: FirebaseFirestore) {

    suspend fun createUserProfile(user: User) {
        firestore.collection(Constants.USERS_COLLECTION).document(user.uid).set(user).await()
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        firestore.collection(Constants.USERS_COLLECTION).document(userId)
            .update("fcmToken", token).await()
    }
}
