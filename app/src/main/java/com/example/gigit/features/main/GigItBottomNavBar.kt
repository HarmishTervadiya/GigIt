package com.example.gigit.features.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gigit.navigation.BottomNavScreen

@Composable
fun GigItBottomNavBar(
    navController: NavHostController,
    onAddGigClick: () -> Unit
) {
    val items = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.ActiveGigs,
        BottomNavScreen.AddGig, // The new central item
        BottomNavScreen.Notifications,
        BottomNavScreen.MyProfile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                label = { Text(screen.label) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                selected = isSelected,
                onClick = {
                    if (screen.route == BottomNavScreen.AddGig.route) {
                        // If the "Add" button is clicked, trigger the lambda
                        onAddGigClick()
                    } else {
                        // Otherwise, perform the standard navigation
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    // You can add a special color for the "Add" button if desired
                    selectedIconColor = if (screen.route == BottomNavScreen.AddGig.route && !isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}