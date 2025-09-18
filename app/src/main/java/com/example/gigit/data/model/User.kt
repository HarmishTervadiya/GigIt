package com.example.gigit.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId
    val uid: String = "",
    val username: String = "",
    val mobileNumber: String = "",
    val profileImageUrl: String = "",
    val upiId: String = "",
    val gamificationPoints: Long = 0, // Using Long for numbers in Firestore
    val paymentSuccessRate: Double = 100.0,
    val totalReviewsAsPoster: Long = 0,
    val tasksPostedCount: Long = 0,
    val tasksCompletedCount: Long = 0,
    @ServerTimestamp
    val createdAt: Date? = null
)