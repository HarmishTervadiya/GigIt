package com.example.gigit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gigit.R
import com.example.gigit.data.model.User

@Composable
fun ReviewSheetContent(
    userToReview: User,
    isTasker: Boolean,
    onSubmitReview: (rating: Int, comment: String, paymentSuccess: Boolean?) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var paymentSuccess by remember { mutableStateOf<Boolean?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
        ,
        horizontalAlignment = Alignment.Start
    ) {
        // Handle
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Surface(
                modifier = Modifier.size(width = 40.dp, height = 4.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = White
            ) {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Leave a Review", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // User Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = userToReview.profileImageUrl.ifEmpty { R.drawable.onboarding_image_2 },
                contentDescription = "User to review",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(userToReview.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Star Rating Input
        Text("Your Rating", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row {
            (1..5).forEach { index ->
                IconButton(onClick = { rating = index }) {
                    Icon(
                        imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Rate $index",
                        tint = if (index <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Comment Input
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Write a review (optional)") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Payment Verification (only for Tasker)
        if (isTasker) {
            Text("Payment Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Did you receive the payment successfully?", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { paymentSuccess = true }, colors = ButtonDefaults.buttonColors(containerColor = if(paymentSuccess == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("Yes")
                }
                Button(onClick = { paymentSuccess = false }, colors = ButtonDefaults.buttonColors(containerColor = if(paymentSuccess == false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("No")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Submit Button
        Button(
            onClick = { onSubmitReview(rating, comment, if (isTasker) paymentSuccess else null) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = rating > 0 && (!isTasker || paymentSuccess != null) // Enable only if rating is given and payment is confirmed
        ) {
            Text("Submit Review")
        }
    }
}
