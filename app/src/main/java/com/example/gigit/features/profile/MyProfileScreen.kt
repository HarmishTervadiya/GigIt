package com.example.gigit.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(mainNavController: NavController) {
    val viewModel: MyProfileViewModel = viewModel(factory = MyProfileViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = { mainNavController.navigate(Screen.EditProfile.route) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
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
                    Text(uiState.error!!)
                }
            }
            uiState.user != null -> {
                // Here you would build the UI to display the user's profile,
                // including their username, stats, and a tab for their reviews.
                // You can also add the logout button here.
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    Text("Welcome, ${uiState.user!!.username}")
                    // ... More UI components
                    Button(onClick = {
                        viewModel.signOut()
                        // Navigate back to the auth flow after logout
                        mainNavController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
