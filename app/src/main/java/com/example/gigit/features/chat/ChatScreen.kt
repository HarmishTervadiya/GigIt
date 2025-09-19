package com.example.gigit.features.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gigit.R
import com.example.gigit.data.model.Message
import com.example.gigit.data.model.Task
import com.example.gigit.data.model.User
import com.example.gigit.util.Constants
import androidx.core.net.toUri
import com.example.gigit.navigation.Screen
import com.example.gigit.ui.components.ReviewSheetContent
import com.example.gigit.ui.components.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    taskId: String,
    navController: NavController
) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(taskId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val task = uiState.task
    val isPoster = uiState.currentUser?.uid == uiState.task?.posterId
    val isTasker = uiState.currentUser?.uid == task?.taskerId
    val isTaskFullyCompleted = task?.status == Constants.TASK_STATUS_COMPLETED && task.paymentStatus == "SUCCESS"

    LaunchedEffect(task?.status, task?.paymentStatus, isPoster) {
        if (task?.status == Constants.TASK_STATUS_COMPLETED &&
            (task.paymentStatus == "PENDING" || task.paymentStatus == "FAILED") &&
            isPoster
        ) {
            navController.navigate(Screen.Payment.createRoute(taskId)) {
                popUpTo(Screen.Chat.createRoute(taskId)) { inclusive = true }
            }
        }
    }

//    if (uiState.showPostGigDialog && uiState.task != null && uiState.currentUser != null) {
//        PostGigDialog(
//            task = uiState.task!!,
//            currentUserId = uiState.currentUser!!.uid,
//            onDismiss = { viewModel.dismissPostGigDialog() },
//            onSubmit = { rating, comment, complain ->
//                viewModel.submitReviewAndComplaint(rating, comment, complain)
//            }
//        )
//    }

    if (uiState.showReviewSheet && uiState.otherUser != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissReviewSheet() },
            containerColor = White
        ) {
            ReviewSheetContent(
                userToReview = uiState.otherUser!!,
                isTasker = isTasker,
                onSubmitReview = { rating, comment, paymentSuccess ->
                    viewModel.submitReview(rating, comment, paymentSuccess)
                }
            )
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.otherUser?.username ?: "Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isTaskFullyCompleted) {
                        TextButton(onClick = { viewModel.onLeaveReviewClicked() }) {
                            Text("Leave a Review")
                        }
                    }
                    IconButton(onClick = {
                        val mobileNumber = uiState.otherUser?.mobileNumber
                        if (!mobileNumber.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$mobileNumber")
                            }
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Task Status Bar
            TaskStatusBar(
                taskStatus = uiState.task?.status ?: "",
                isTasker = uiState.currentUser?.uid == uiState.task?.taskerId,
                onMarkCompleteClick = { viewModel.onMarkCompleteClicked() },
                paymentStatus = task?.paymentStatus ?: "PENDING"
            )

            // Main chat content with weight to take available space
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = uiState.error!!)
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)

                        , // Takes remaining space above input
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp), // Reduced from 12dp
                        reverseLayout = false,

                    ) {
                        items(uiState.messages, key = { it.timestamp.toString() }) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == viewModel.getCurrentUserId(),
                                currentUser = uiState.currentUser,
                                otherUser = uiState.otherUser
                            )
                        }
                        // Add extra space at bottom so last message isn't too close to input
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Input bar at bottom - always visible
            if (task != null) {
                if(task.paymentStatus != "SUCCESS" && task.status != Constants.TASK_STATUS_COMPLETED) {

                    ChatInputBar(
                        onSendMessage = {
                            viewModel.sendMessage(it)
                            // Auto-scroll to bottom when sending message
        //                    LaunchedEffect(Unit) {
        //                    if (uiState.messages.isNotEmpty()) {
        //
        //                            listState.animateScrollToItem(uiState.messages.size)
        //                        }
        //                    }
                        }
                    )
                }
            }

            LaunchedEffect(uiState.messages.size) {
                if (uiState.messages.isNotEmpty()) {
                    listState.animateScrollToItem(uiState.messages.size - 1)
                }
            }
        }
    }
}

@Composable
private fun TaskStatusBar(
    taskStatus: String,
    paymentStatus: String,
    isTasker: Boolean,
    onMarkCompleteClick: () -> Unit
) {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusText = when {
                    taskStatus == Constants.TASK_STATUS_COMPLETED && paymentStatus == "SUCCESS" -> "Task & Payment Complete"
                    taskStatus == Constants.TASK_STATUS_AWAITING_PAYMENT -> "Awaiting Payment"
                    taskStatus == Constants.TASK_STATUS_RESERVED -> "Task in progress"
                    else -> "Task Active"
                }
                val statusIcon = when {
                    taskStatus == Constants.TASK_STATUS_COMPLETED && paymentStatus == "SUCCESS" -> Icons.Default.TaskAlt
                    taskStatus == Constants.TASK_STATUS_AWAITING_PAYMENT -> Icons.Default.HourglassEmpty
                    else -> Icons.Default.Timer
                }

                Icon(statusIcon, contentDescription = "Status", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(statusText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            }

            if (isTasker && taskStatus == Constants.TASK_STATUS_RESERVED) {
                TextButton(onClick = onMarkCompleteClick) {
                    Text("Mark Complete")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    currentUser: User?,
    otherUser: User?
) {
    val arrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val userToShow = if (isCurrentUser) currentUser else otherUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            AsyncImage(
                model = userToShow?.profileImageUrl?.ifEmpty { R.drawable.onboarding_image_2 },
                contentDescription = "Sender Avatar",
                modifier = Modifier.size(28.dp).clip(CircleShape), // Reduced from 32dp
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp)) // Reduced from 8dp
        }

        Column {
            if (!isCurrentUser) {
                Text(
                    text = userToShow?.username ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isCurrentUser) 16.dp else 4.dp,
                            topEnd = if (isCurrentUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp) // Reduced from 12dp, 8dp
            ) {
                Text(
                    text = message.text,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(6.dp)) // Reduced from 8dp
            AsyncImage(
                model = userToShow?.profileImageUrl?.ifEmpty { R.drawable.onboarding_image_1 },
                contentDescription = "Sender Avatar",
                modifier = Modifier.size(28.dp).clip(CircleShape), // Reduced from 32dp
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ChatInputBar(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp
                ), // Add navigation bar padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(20.dp), // Reduced from 24dp
                maxLines = 4, // Allow multiline but limit height
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun PostGigDialog(
    task: Task,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String, fileComplaint: Boolean) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var fileComplaint by remember { mutableStateOf(false) }
    val isPoster = task.posterId == currentUserId
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isPoster) "Complete Payment & Review" else "Confirm Payment & Review") },
        text = {
            Column {
                // --- PAYMENT GATEWAY DEMO ---
                if (isPoster) {
                    Text("Step 1: Complete Payment", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    // This button demonstrates the planned UPI integration
                    Button(onClick = {
                        val upiIntent = Intent(
                            Intent.ACTION_VIEW,
                            "upi://pay?pa=${"tasker-upi@placeholder"}&pn=Tasker&am=${task.rewardAmount}&cu=INR&tn=Payment for ${task.title}".toUri()
                        )
                        context.startActivity(upiIntent)
                    }) {
                        Text("Pay ₹${task.rewardAmount.toInt()} with UPI")
                    }
                    OutlinedButton(onClick = { /* Placeholder */ }, enabled = false) {
                        Text("Pay with Card (Coming Soon)")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- REVIEW & COMPLAINT SYSTEM DEMO ---
                Text(if (isPoster) "Step 2: Review Tasker" else "Confirm Payment & Review", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // You can add a more visual star rating component here later
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Leave a review (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = fileComplaint, onCheckedChange = { fileComplaint = it })
                    Text("Report an issue with this gig")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment, fileComplaint) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}