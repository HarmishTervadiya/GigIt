package com.example.gigit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gigit.features.auth.AuthScreen
import com.example.gigit.features.auth.OnboardingScreen
import com.example.gigit.features.auth.SplashScreen
import com.example.gigit.features.chat.ChatScreen
import com.example.gigit.features.main.MainScreen
import com.example.gigit.features.payment.PaymentScreen
import com.example.gigit.features.profile.EditProfileScreen
import com.example.gigit.features.profile.UserProfileScreen
import com.example.gigit.features.taskDetails.TaskDetailsScreen
import com.example.gigit.ui.components.RouteGuard

@Composable
fun AppNavigation(startTaskId: String? = null) {
    val navController = rememberNavController()

    val startDestination = if (startTaskId != null) {
        Screen.Payment.createRoute(startTaskId)
    } else {
        Screen.Splash.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        // --- Pre-Login Flow ---
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController, startTaskId = startTaskId)
        }
        composable(route = Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(route = Screen.Auth.route) { AuthScreen(navController) }

        // --- Post-Login Container ---
        composable(route = Screen.Main.route) {
            MainScreen(mainNavController = navController)
        }

        composable(route = Screen.Payment.route, arguments = Screen.Payment.arguments) { backStackEntry ->
            RouteGuard(
                argument = backStackEntry.arguments?.getString("taskId"),
                navController = navController,
                errorMessage = "Task for payment not found."
            ) { taskId ->
                PaymentScreen(taskId = taskId, navController = navController)
            }
        }

        // --- Screens Without Bottom Nav Bar ---
        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(
            route = Screen.TaskDetails.route,
            arguments = Screen.TaskDetails.arguments
        ) { backStackEntry ->
            RouteGuard(
                argument = backStackEntry.arguments?.getString("taskId"),
                navController = navController,
                errorMessage = "Task not found. Navigating back."
            ) { taskId ->
                TaskDetailsScreen(taskId = taskId, navController = navController)
            }
        }
        composable(route = Screen.Chat.route, arguments = Screen.Chat.arguments) { backStackEntry ->
            RouteGuard(
                argument = backStackEntry.arguments?.getString("taskId"),
                navController = navController,
                errorMessage = "User not found. Navigating back."
            ) { taskId ->
                ChatScreen(navController = navController, taskId = taskId)

            }
        }
        composable(
            route = Screen.UserProfile.route,
            arguments = Screen.UserProfile.arguments
        ) { backStackEntry ->
            RouteGuard(
                argument = backStackEntry.arguments?.getString("userId"),
                navController = navController,
                errorMessage = "User not found. Navigating back."
            ) { userId ->
                UserProfileScreen(userId = userId, navController = navController)
            }
        }
    }
}