package com.example.gigit.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Task(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null, // Optional
    val rewardType: String = "",
    val rewardAmount: Double = 0.0,
    val status: String = "",
    val category: String = "Other",

    val posterId: String = "",
    val taskerId: String? = null, // Null until a tasker accepts
    val participantIds: List<String> = emptyList(), // For querying active gigs
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