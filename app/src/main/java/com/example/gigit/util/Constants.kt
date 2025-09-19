package com.example.gigit.util

object Constants {
    // Firestore Collection Names
    const val USERS_COLLECTION = "users"
    const val TASKS_COLLECTION = "tasks"
    const val REVIEWS_COLLECTION = "reviews"
    const val NOTIFICATIONS_COLLECTION = "notifications"
    const val TASK_CHATS_COLLECTION = "taskChats"

    // Task Statuses
    const val TASK_STATUS_OPEN = "OPEN"
    const val TASK_STATUS_RESERVED = "RESERVED"
    const val TASK_STATUS_AWAITING_PAYMENT = "PENDING"
    const val TASK_STATUS_COMPLETED = "COMPLETED"
    const val TASK_STATUS_CANCELLED = "CANCELLED"
    const val TASK_STATUS_EXPIRED = "EXPIRED"

    // Reward Types
    const val REWARD_TYPE_CASH = "CASH"
    const val REWARD_TYPE_FAVOR = "FAVOR"
}