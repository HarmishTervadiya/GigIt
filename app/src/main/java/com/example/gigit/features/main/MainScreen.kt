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
import com.example.gigit.features.activeGigs.ActiveGigsScreen
import com.example.gigit.features.feed.HomeScreen
import com.example.gigit.features.notifications.NotificationsScreen
import com.example.gigit.features.profile.MyProfileScreen
import com.example.gigit.navigation.BottomNavScreen
import com.example.gigit.ui.components.AddGigSheetContent
import com.example.gigit.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavController) {
    val bottomNavController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        // The Scaffold is now much cleaner, with no FAB logic
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
                // Note: The "AddGig" route is not needed here as it's an action, not a screen.
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
                     // 1. Get the current user
                     val currentUser = Firebase.auth.currentUser
                     if (currentUser != null) {
                         // 2. Create the Task object from the form data
                         val newTask = Task(
                             title = title,
                             description = description,
                             rewardType = Constants.REWARD_TYPE_CASH,
                             rewardAmount = amount.toDoubleOrNull() ?: 0.0,
                             status = Constants.TASK_STATUS_OPEN,
                             posterId = currentUser.uid,
                             posterUsername = currentUser.displayName
                                 ?: "A User", // Or fetch from your user profile
                             locationString = location
                             // createdAt is handled by the server via @ServerTimestamp
                         )
                         // 3. Call the repository to save the new task
//                            taskRepository.postNewTask(newTask)
                     }

                     // 4. Hide the bottom sheet after the work is done
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