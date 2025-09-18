package com.example.gigit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gigit.data.model.Task
import com.example.gigit.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ActiveTaskCard(
    task: Task,
    userRepository: UserRepository,
    onClick: () -> Unit
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    var otherUserName by remember { mutableStateOf<String?>(null) }

    // This effect will run once to fetch the other user's profile
    LaunchedEffect(task.id) {
        val otherUserId = if (currentUserId == task.posterId) {
            task.taskerId
        } else {
            task.posterId
        }

        if (otherUserId != null) {
            val userProfile = userRepository.getUserProfile(otherUserId)
            otherUserName = userProfile?.username
        }
    }

    // Determine the text to display based on the fetched data
    val otherUserText = when {
        otherUserName != null -> "Chat with $otherUserName"
        currentUserId == task.posterId -> "Chat with Tasker..." // Loading state
        else -> "Chat with ${task.posterUsername}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = otherUserText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

