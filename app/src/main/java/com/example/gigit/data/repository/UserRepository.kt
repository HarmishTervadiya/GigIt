package com.example.gigit.data.repository

import com.example.gigit.data.model.User
import com.example.gigit.data.source.UserSource

class UserRepository(private val source: UserSource) {

    suspend fun createUserProfile(user: User) {
        source.createUserProfile(user)
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        source.updateFcmToken(userId, token)
    }
}
