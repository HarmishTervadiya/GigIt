package com.example.gigit.features.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.navigation.Screen
import com.example.gigit.ui.components.TaskCard
import com.example.gigit.ui.theme.BackgroundGray
import com.example.gigit.ui.theme.Black
import com.example.gigit.ui.theme.BluePrimary
import com.example.gigit.ui.theme.BorderGray
import com.example.gigit.ui.theme.TextBlack
import com.example.gigit.ui.theme.TextGray
import com.example.gigit.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Client-side search filtering
    val filteredTasks = remember(uiState.tasks, uiState.searchQuery, uiState.selectedCategory) {
        var filtered = uiState.tasks

        // Apply search filter
        if (uiState.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(uiState.searchQuery, ignoreCase = true) ||
                        it.description.contains(uiState.searchQuery, ignoreCase = true)
            }
        }

        // Apply category filter
        if (uiState.selectedCategory != "All") {
            filtered = filtered.filter { it.category == uiState.selectedCategory }
        }

        filtered
    }

    // Detect scroll state for header visibility
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Collapsible Header
        AnimatedVisibility(
            visible = !isScrolled,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Surface(
                color = White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Compact Header
                    Text(
                        text = "Find Your Next Perfect Gig",
                        style = MaterialTheme.typography.titleMedium,
                        color = Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compact Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        placeholder = {
                            Text(
                                "Search gigs...",
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChanged("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderGray,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        ),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compact View Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BorderGray)
                        ) {
                            Surface(
                                color = if (uiState.viewMode == ViewMode.LIST) White else Color.Transparent,
                                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                TextButton(
                                    onClick = { viewModel.onViewModeSelected(ViewMode.LIST) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = TextGray),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.ViewList,
                                        contentDescription = "List View",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("List", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Surface(
                                color = if (uiState.viewMode == ViewMode.MAP) White else Color.Transparent,
                                shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                TextButton(
                                    onClick = { viewModel.onViewModeSelected(ViewMode.MAP) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = TextGray),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(
                                        Icons.Outlined.Map,
                                        contentDescription = "Map View",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Map", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Filters - Show persistently for all states
        if (uiState.categories.isNotEmpty() && !uiState.isLoading) {
            Surface(
                color = White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(uiState.categories) { category ->
                            FilterChip(
                                selected = uiState.selectedCategory == category,
                                onClick = { viewModel.onCategorySelected(category) },
                                label = {
                                    Text(
                                        category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (uiState.selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = White,
                                    containerColor = White,
                                    labelColor = TextGray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = BorderGray,
                                    selectedBorderColor = BluePrimary,
                                    borderWidth = 1.dp,
                                    enabled = true,
                                    selected = uiState.selectedCategory == category,
                                ),
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }

                    // Results count and clear filters
                    if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != "All") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.error == null) "${filteredTasks.size} gigs found" else "Filters applied",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextBlack
                            )

                            TextButton(
                                onClick = {
                                    viewModel.onSearchQueryChanged("")
                                    viewModel.onCategorySelected("All")
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    "Clear filters",
                                    color = BluePrimary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = BluePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Finding perfect gigs for you...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Oops! Something went wrong",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Retry logic */ },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Try Again", color = White)
                        }
                    }
                }
            }

            filteredTasks.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "🔍",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No gigs found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != "All") {
                                "Try adjusting your search or filters above"
                            } else {
                                "No gigs are currently available"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            uiState.viewMode == ViewMode.LIST -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Task items
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onCardClick = {
                                mainNavController.navigate(Screen.TaskDetails.createRoute(task.id))
                            }
                        )
                    }
                }
            }

            uiState.viewMode == ViewMode.MAP -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Map",
                            tint = BluePrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Map View",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Coming Soon! 🗺️",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.onViewModeSelected(ViewMode.LIST) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary)
                        ) {
                            Text("Back to List View")
                        }
                    }
                }
            }
        }
    }
}