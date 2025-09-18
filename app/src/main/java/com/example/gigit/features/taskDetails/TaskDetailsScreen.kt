package com.example.gigit.features.task_details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    navController: NavController
) {
    val viewModel: TaskDetailsViewModel = viewModel(factory = TaskDetailsViewModelFactory(taskId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to chat screen on successful acceptance
    LaunchedEffect(uiState.acceptSuccess) {
        if (uiState.acceptSuccess) {
            navController.navigate(Screen.Chat.createRoute(taskId)) {
                // Clear the back stack up to the feed
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
            // Show accept button only if the task is loaded and not already accepted
            if (uiState.task != null && !uiState.acceptSuccess) {
                Button(
                    onClick = { viewModel.acceptTask() },
                    enabled = !uiState.isAccepting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (uiState.isAccepting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Accept Gig")
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!)
                }
            }
            uiState.task != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    val task = uiState.task!!
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Posted by: ${task.posterUsername}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(task.description, style = MaterialTheme.typography.bodyLarge)
                    // ... Add more details like reward, location, etc.
                }
            }
        }
    }
}
