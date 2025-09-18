package com.example.gigit.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)