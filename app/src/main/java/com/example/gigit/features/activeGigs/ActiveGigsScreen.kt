package com.example.gigit.features.activeGigs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.data.model.Task
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.UserSource
import com.example.gigit.navigation.Screen
import com.example.gigit.ui.components.ActiveTaskCard
import com.example.gigit.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiveGigsScreen(mainNavController: NavController) {
    val viewModel: ActiveGigsViewModel = viewModel(factory = ActiveGigsViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val currentUserId = Firebase.auth.currentUser?.uid

    // Sync pager state with ViewModel
    LaunchedEffect(uiState.selectedTabIndex) {
        pagerState.animateScrollToPage(uiState.selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

        Column(modifier = Modifier.padding(4.dp)) {
            TabRow(selectedTabIndex = uiState.selectedTabIndex) {
                Tab(
                    selected = uiState.selectedTabIndex == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("My Gigs") }
                )
                Tab(
                    selected = uiState.selectedTabIndex == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Accepted") }
                )
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = uiState.error!!, textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    HorizontalPager(state = pagerState) { page ->
                        val gigs = if (page == 0) uiState.postedGigs else uiState.acceptedGigs
                        if (gigs.isEmpty()) {
                            EmptyState(page = page)
                        } else {
                            GigsList(gigs = gigs, mainNavController = mainNavController, currentUserId = currentUserId)
                        }
                    }
                }
            }
        }
    }


@Composable
private fun GigsList(gigs: List<Task>, mainNavController: NavController, currentUserId: String?) {
    val userRepository = remember { UserRepository(UserSource(Firebase.firestore)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(gigs, key = { it.id }) { task ->
            ActiveTaskCard(
                task = task,
                userRepository = userRepository,
                onClick = {
                    if (task.status == Constants.TASK_STATUS_AWAITING_PAYMENT && task.posterId == currentUserId) {
                        // If payment is pending and I am the poster, go directly to payment.
                        mainNavController.navigate(Screen.Payment.createRoute(task.id))
                    } else {
                        // Otherwise, go to the chat screen.
                        mainNavController.navigate(Screen.Chat.createRoute(task.id))
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyState(page: Int) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (page == 0) "You haven't posted any active gigs." else "You haven't accepted any gigs yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

