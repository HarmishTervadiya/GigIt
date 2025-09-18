package com.example.gigit.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val taskId: String = "",
    val reviewerId: String = "",
    val revieweeId: String = "",
    val rating: Int = 0,
    val comment: String = "",

    // This field ONLY exists when a Tasker reviews a Poster
    val paymentCompletedSuccessfully: Boolean? = null,

    @ServerTimestamp
    val createdAt: Date? = null
)