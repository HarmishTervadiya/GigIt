package com.example.gigit.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
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
import com.example.gigit.navigation.Screen
import com.example.gigit.ui.components.ReviewItem
import com.example.gigit.ui.components.SettingsItem
import com.example.gigit.ui.components.StatCard
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyProfileScreen(mainNavController: NavController) {
    val viewModel: MyProfileViewModel = viewModel(factory = MyProfileViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userRepository = remember { UserRepository(UserSource(Firebase.firestore)) }

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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    AsyncImage(
                        model = user.profileImageUrl.ifEmpty { R.drawable.onboarding_image_2 },
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

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsItem(icon = Icons.Default.Edit, title = "Edit Profile", onClick = { mainNavController.navigate(Screen.EditProfile.route) })
                        SettingsItem(icon = Icons.Default.Notifications, title = "Notifications", onClick = { /* TODO */ })
                        SettingsItem(icon = Icons.Default.Payment, title = "Payment Methods", onClick = { /* TODO */ })
                        SettingsItem(icon = Icons.Default.Help, title = "Help & Support", onClick = { /* TODO */ })
                        SettingsItem(icon = Icons.AutoMirrored.Filled.Logout, title = "Logout", onClick = {
                            viewModel.signOut()
                            mainNavController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        })
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

