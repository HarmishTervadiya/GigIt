package com.example.gigit.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gigit.R
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.UserSource
import com.example.gigit.ui.components.ReviewItem
import com.example.gigit.ui.components.StatCard
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavController
) {
    val viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModelFactory(userId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userRepository = remember { UserRepository(UserSource(Firebase.firestore)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.user?.username ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                val user = uiState.user!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        AsyncImage(
                            model = user.profileImageUrl.ifEmpty { R.drawable.onboarding_image_1 },
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(90.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(user.username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        user.createdAt?.let {
                            val formattedDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it)
                            Text(
                                text = "Member since $formattedDate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(label = "Tasks Done", value = user.tasksCompletedCount.toString(), modifier = Modifier.weight(1f))
                            StatCard(label = "Tasks Posted", value = user.tasksPostedCount.toString(), modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                            StatCard(label = "Reviews", value = uiState.reviews.size.toString(), modifier = Modifier.fillMaxWidth())
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (uiState.reviews.isNotEmpty()) {
                        item {
                            Text(
                                "Reviews Received",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(uiState.reviews) { review ->
                            ReviewItem(review = review, userRepository = userRepository)
                        }
                    }
                }
            }
        }
    }
}

