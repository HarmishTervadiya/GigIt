package com.example.gigit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * A composable that "guards" a route. It checks if a required argument is valid.
 * If valid, it displays the screen's content.
 * If invalid (null or blank), it shows a custom error message and navigates back.
 */
@Composable
fun RouteGuard(
    argument: String?,
    navController: NavController,
    errorMessage: String = "Content not found. Please try again.",
    content: @Composable (id: String) -> Unit
) {
    if (argument.isNullOrBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(errorMessage)
        }
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    } else {
        content(argument)
    }
}