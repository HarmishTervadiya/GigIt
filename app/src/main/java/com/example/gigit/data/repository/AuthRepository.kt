package com.example.gigit.data.repository

import com.example.gigit.data.model.User
import com.example.gigit.data.source.AuthSource
import com.example.gigit.util.Resource
import com.google.firebase.auth.FirebaseUser

class AuthRepository(
    private val source: AuthSource,
    private val userRepository: UserRepository
) {

    fun isUserAuthenticated(): Boolean = source.isUserAuthenticated()
    fun getCurrentUserId(): String? = source.getCurrentUserId()
    fun signOut() = source.signOut()

    suspend fun login(email: String, pass: String): Resource<FirebaseUser?> {
        return source.login(email, pass)
    }

    suspend fun signUp(email: String, pass: String, username: String): Resource<FirebaseUser?> {
        return when (val authResult = source.signUp(email, pass, username)) {
            is Resource.Success -> {
                authResult.data?.let { firebaseUser ->
                    val newUser = User(
                        uid = firebaseUser.uid,
                        username = username,
                        email = firebaseUser.email ?: ""
                    )
                    userRepository.createUserProfile(newUser)
                    Resource.Success(firebaseUser)
                } ?: Resource.Error("User authentication succeeded but user data was null.")
            }
            is Resource.Error -> {
                authResult // Pass the original error message up
            }
            is Resource.Loading -> {
                // While the source won't return this, we must handle it.
                // Returning an error is a safe way to manage this unexpected state.
                Resource.Error("An unexpected loading state occurred.")
            }
        }
    }
}

