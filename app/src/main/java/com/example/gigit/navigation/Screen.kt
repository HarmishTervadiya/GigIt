package com.example.gigit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

// Defines all top-level navigation destinations in the app
sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Auth : Screen("auth_screen")
    object Main : Screen("main_screen") // Container for screens with the bottom nav bar

    object Payment : Screen(
        route = "payment_screen/{taskId}",
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
    ) {
        fun createRoute(taskId: String) = "payment_screen/$taskId"
    }

    // --- Screens accessible from the main graph that DON'T have a bottom nav bar ---
    object EditProfile : Screen("edit_profile_screen")

    object TaskDetails : Screen(
        route = "task_details/{taskId}",
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
    ) {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }

    object Chat : Screen(
        route = "chat/{taskId}",
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
    ) {
        fun createRoute(taskId: String) = "chat/$taskId"
    }

    object UserProfile : Screen(
        route = "user_profile/{userId}",
        arguments = listOf(navArgument("userId") { type = NavType.StringType })
    ) {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
}

sealed class BottomNavScreen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavScreen("home", "Home", Icons.Default.Home)
    object ActiveGigs : BottomNavScreen("active_gigs", "Active", Icons.AutoMirrored.Filled.List)
    object AddGig : BottomNavScreen("add_gig_action", "Add", Icons.Default.Add)
    object Notifications : BottomNavScreen("notifications", "Notifications", Icons.Default.Notifications)
    object MyProfile : BottomNavScreen("my_profile", "Profile", Icons.Default.Person)
}