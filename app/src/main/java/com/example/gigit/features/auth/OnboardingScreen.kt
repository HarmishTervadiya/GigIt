package com.example.gigit.features.auth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gigit.R
import com.example.gigit.navigation.Screen
import kotlinx.coroutines.launch

// Data class to define each onboarding page's content
data class OnboardingPage(
    val imageResId: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    // Define the content for each of your onboarding pages
    val pages = listOf(
        OnboardingPage(
            imageResId = R.drawable.onboarding_image_1, // Make sure these drawable IDs are correct
            title = "Post a Task in Seconds",
            description = "Need coffee or a printout? Just ask."
        ),
        OnboardingPage(
            imageResId = R.drawable.onboarding_image_2,
            title = "Get Help Instantly",
            description = "Connect with helpers nearby."
        ),
        OnboardingPage(
            imageResId = R.drawable.onboarding_image_3,
            title = "Seamless Completion",
            description = "Confirm tasks with ease. Our platform ensures a smooth process from start to finish."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage by remember { derivedStateOf { pagerState.currentPage == pages.size - 1 } }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up most of the vertical space
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            // Page Indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }

            // "Get Started" Button (only visible on the last page)
            if (isLastPage) {
                Button(
                    onClick = {
                        // Navigate to the Login screen or directly to Signup
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium, // Using default medium shape
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Get Started", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(32.dp)) // Padding for the bottom
            } else {
                // For non-last pages, show a next button or just padding
                Spacer(modifier = Modifier.height(88.dp)) // To match the height of the "Get Started" button + its padding
            }
        }
    }
}

// Composable for a single page's content
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null, // Content description for accessibility
            modifier = Modifier
                .fillMaxWidth(0.8f) // Image takes 80% width
                .aspectRatio(1f), // Maintain aspect ratio
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}