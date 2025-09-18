package com.example.gigit.features.active_gigs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.UserSource
import com.example.gigit.navigation.Screen
import com.example.gigit.ui.components.ActiveTaskCard
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGigsScreen(mainNavController: NavController) {
    val viewModel: ActiveGigsViewModel = viewModel(factory = ActiveGigsViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Create the UserRepository instance to pass down to the card
    val userRepository = remember {
        UserRepository(UserSource(Firebase.firestore))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Active Gigs") }) }
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
            uiState.activeGigs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "You have no active gigs.")
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = paddingValues) {
                    items(uiState.activeGigs, key = { it.id }) { task ->
                        ActiveTaskCard(
                            task = task,
                            userRepository = userRepository, // Pass the repository to the card
                            onClick = {
                                mainNavController.navigate(Screen.Chat.createRoute(task.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

