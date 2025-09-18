package com.example.gigit.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gigit.data.model.Task
import com.example.gigit.data.repository.TaskRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.TaskSource
import com.example.gigit.data.source.UserSource
import com.example.gigit.features.active_gigs.ActiveGigsScreen
import com.example.gigit.features.feed.HomeScreen
import com.example.gigit.features.notifications.NotificationsScreen
import com.example.gigit.features.profile.MyProfileScreen
import com.example.gigit.navigation.BottomNavScreen
import com.example.gigit.ui.components.AddGigSheetContent
import com.example.gigit.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavController) {
    val bottomNavController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Manually create the repository instances needed for this screen's logic
    val taskRepository = remember { TaskRepository(TaskSource(Firebase.firestore)) }
    val userRepository = remember { UserRepository(UserSource(Firebase.firestore)) }


    Scaffold(
        bottomBar = {
            GigItBottomNavBar(
                navController = bottomNavController,
                onAddGigClick = { showBottomSheet = true }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = bottomNavController, startDestination = BottomNavScreen.Home.route) {
                composable(BottomNavScreen.Home.route) { HomeScreen(mainNavController) }
                composable(BottomNavScreen.ActiveGigs.route) { ActiveGigsScreen(mainNavController) }
                composable(BottomNavScreen.Notifications.route) { NotificationsScreen(mainNavController) }
                composable(BottomNavScreen.MyProfile.route) { MyProfileScreen(mainNavController) }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            AddGigSheetContent(
                onPostGig = { title, description, location, amount ->
                    scope.launch {
                        val currentUser = Firebase.auth.currentUser
                        if (currentUser != null) {
                            // Fetch the full user profile to get the latest data
                            val userProfile = userRepository.getUserProfile(currentUser.uid)

                            val newTask = Task(
                                title = title,
                                description = description,
                                rewardType = Constants.REWARD_TYPE_CASH,
                                rewardAmount = amount.toDoubleOrNull() ?: 0.0,
                                status = Constants.TASK_STATUS_OPEN,
                                posterId = currentUser.uid,
                                // Use fresh data from Firestore profile for consistency
                                posterUsername = userProfile?.username ?: currentUser.displayName ?: "A User",
                                posterPaymentSuccessRate = userProfile?.paymentSuccessRate ?: 100.0,
                                locationString = location
                                // createdAt is handled by the server via @ServerTimestamp
                            )
                            // Call the repository to save the new task
                            taskRepository.postNewTask(newTask)
                        }

                        // Hide the bottom sheet after the work is done
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

