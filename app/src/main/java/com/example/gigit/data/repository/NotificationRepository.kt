package com.example.gigit.data.repository

import com.example.gigit.data.model.AppNotification
import com.example.gigit.data.source.NotificationSource
import com.example.gigit.util.Resource
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val source: NotificationSource) {

    fun getUserNotifications(userId: String): Flow<Resource<List<AppNotification>>> {
        return source.getUserNotifications(userId)
    }

    suspend fun createNotification(notification: AppNotification): Resource<Unit> {
        return source.createNotification(notification)
    }
}
