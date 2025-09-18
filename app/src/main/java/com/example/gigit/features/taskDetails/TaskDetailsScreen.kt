package com.example.gigit.features.taskDetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gigit.R
import com.example.gigit.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    navController: NavController
) {
    val viewModel: TaskDetailsViewModel = viewModel(factory = TaskDetailsViewModelFactory(taskId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showUpiDialog) {
        UpiIdInputDialog(
            onDismiss = { viewModel.dismissUpiDialog() },
            onConfirm = { upiId -> viewModel.saveUpiAndAcceptTask(upiId) }
        )
    }

    LaunchedEffect(uiState.acceptSuccess) {
        if (uiState.acceptSuccess) {
            navController.navigate(Screen.Chat.createRoute(taskId)) {
                popUpTo(Screen.Main.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.task != null && !uiState.acceptSuccess) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { viewModel.onAcceptClick() },
                        enabled = !uiState.isAccepting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                    ) {
                        if (uiState.isAccepting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Accept Task")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = uiState.error!!) }
            }
            uiState.task != null && uiState.poster != null -> {

                val task = uiState.task!!
                val poster = uiState.poster!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Task Header (Title & Reward) ---
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Reward: ₹${task.rewardAmount.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )


                    // --- Poster Section ---
                    Text("Posted by", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.UserProfile.createRoute(poster.uid)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text(poster.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${poster.paymentSuccessRate.toInt()}% (${poster.totalReviewsAsPoster} reviews)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        AsyncImage(
                            model = poster.profileImageUrl.ifEmpty { R.drawable.onboarding_image_1 },
                            contentDescription = "Poster profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    // --- Task Info Section ---
                    Text("Task Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailInfoRow(icon = Icons.Default.Category, title = "Category", subtitle = task.category)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailInfoRow(icon = Icons.Default.LocationOn, title = "Location", subtitle = task.locationString)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(task.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun UpiIdInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var upiId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Your UPI ID")
        },
        text = {
            Column {
                Text("To receive payments for cash gigs, please provide your UPI ID.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("your-name@bank") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (upiId.isNotBlank()) {
                        onConfirm(upiId)
                    }
                },
                enabled = upiId.isNotBlank() && upiId.contains("@") // Basic validation
            ) {
                Text("Save & Accept Gig")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}