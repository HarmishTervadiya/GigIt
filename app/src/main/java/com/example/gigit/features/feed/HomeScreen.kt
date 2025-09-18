package com.example.gigit.features.feed

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.navigation.Screen
import com.example.gigit.ui.components.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Open Gigs") })
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
                    Text(text = uiState.error ?: "An unknown error occurred.")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues
                ) {
                    items(uiState.tasks, key = { it.id }) { task -> // FIX: Use the unique ID for the key
                        TaskCard(
                            task = task,
                            onCardClick = {
                                mainNavController.navigate(Screen.TaskDetails.createRoute(task.id))
                            },
                            onLocationClick = { location ->
                                // FIX: Added logic to handle location chip clicks
                                val mapIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=$location")
                                )
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

