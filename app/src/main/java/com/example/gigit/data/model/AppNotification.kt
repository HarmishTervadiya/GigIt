package com.example.gigit.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AppNotification(
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "", //"TASK_ACCEPTED", "NEW_MESSAGE"
    val isRead: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)