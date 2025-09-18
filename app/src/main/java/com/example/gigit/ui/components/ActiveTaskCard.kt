package com.example.gigit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gigit.R
import com.example.gigit.data.model.Task
import com.example.gigit.data.model.User
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
    var otherUser by remember { mutableStateOf<User?>(null) }

    // This effect fetches the full profile of the other user
    LaunchedEffect(task.id) {
        val otherUserId = if (currentUserId == task.posterId) task.taskerId else task.posterId
        if (otherUserId != null) {
            otherUser = userRepository.getUserProfile(otherUserId)
        }
    }

    // Determine the text to display based on the fetched data
    val otherUserText = when {
        otherUser != null -> "Chat with ${otherUser?.username}"
        currentUserId == task.posterId -> "Chat with Tasker..." // Loading state
        else -> "Chat with ${task.posterUsername}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clickable(onClick = onClick)
        ,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardColors(containerColor = Color.White, contentColor = Color.Black, disabledContainerColor = Color.Gray, disabledContentColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column for text content on the left
            Column(Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Status: ${task.status}", // e.g., "Status: RESERVED"
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = otherUserText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Profile image of the other user on the right
            AsyncImage(
                model = otherUser?.profileImageUrl?.ifEmpty { R.drawable.onboarding_image_2 } ?: R.drawable.onboarding_image_2,
                contentDescription = "Profile picture of the other user",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }
    }
}

