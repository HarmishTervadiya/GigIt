package com.example.gigit.data.source

import com.example.gigit.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthSource(private val auth: FirebaseAuth) {

    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun login(email: String, pass: String): Resource<FirebaseUser?> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(auth.currentUser)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred.")
        }
    }

    suspend fun signUp(email: String, pass: String, username: String): Resource<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }
                user.updateProfile(profileUpdates).await()
                Resource.Success(user)
            } else {
                Resource.Error("User creation failed: user object is null.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred.")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

