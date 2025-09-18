package com.example.gigit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gigit.R
import com.example.gigit.data.model.Message
import com.example.gigit.data.model.User

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    currentUser: User?,
    otherUser: User?
) {
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val backgroundColor =  MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val userToShow = if (isCurrentUser) currentUser else otherUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            AsyncImage(
                model = userToShow?.profileImageUrl?.ifEmpty { R.drawable.onboarding_image_1 } ?: R.drawable.onboarding_image_1,
                contentDescription = "Sender Avatar",
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            if (!isCurrentUser) {
                Text(
                    text = userToShow?.username ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(text = message.text, color = textColor)
            }
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = userToShow?.profileImageUrl?.ifEmpty { R.drawable.onboarding_image_2 } ?: R.drawable.onboarding_image_2,
                contentDescription = "Sender Avatar",
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

