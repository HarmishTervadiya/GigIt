package com.example.gigit.data.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Task(
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null, // Optional
    val rewardType: String = "",
    val rewardAmount: Double = 0.0,
    val status: String = "",

    val posterId: String = "",
    val taskerId: String? = null, // Null until a tasker accepts

    // Denormalized data for fast feed loading
    val posterUsername: String = "",
    val posterPaymentSuccessRate: Double = 0.0,

    // Location data
    val locationString: String = "", // e.g., "Library, Ground Floor"
    val locationGeoPoint: GeoPoint? = null, // For geo-queries

    // Timestamps
    @ServerTimestamp
    val createdAt: Date? = null,
    val acceptedAt: Date? = null,
    val completedAt: Date? = null
)