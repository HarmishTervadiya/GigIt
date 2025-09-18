package com.example.gigit.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Message
import com.example.gigit.data.model.Task
import com.example.gigit.data.model.User
import com.example.gigit.data.repository.AuthRepository
import com.example.gigit.data.repository.TaskRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.AuthSource
import com.example.gigit.data.source.TaskSource
import com.example.gigit.data.source.UserSource
import com.example.gigit.util.Resource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val currentUser: User? = null,
    val otherUser: User? = null,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val showPostGigDialog: Boolean = false,
    val reviewSubmitted: Boolean = false
)

class ChatViewModel(
    private val taskId: String,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTaskAndUserDetails()
        listenForMessages()
    }

    private fun loadTaskAndUserDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
                return@launch
            }

            // Fetch current user's profile for avatar
            val currentUserProfile = userRepository.getUserProfile(currentUserId)

            val taskResult = taskRepository.getTaskDetails(taskId)
            if (taskResult is Resource.Success && taskResult.data != null) {
                val task = taskResult.data
                val otherUserId = if (currentUserId == task.posterId) task.taskerId else task.posterId

                if (otherUserId != null) {
                    val otherUser = userRepository.getUserProfile(otherUserId)
                    _uiState.update { it.copy(isLoading = false, task = task, otherUser = otherUser, currentUser = currentUserProfile) }
                } else {
                    _uiState.update { it.copy(isLoading = false, task = task, currentUser = currentUserProfile) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Task not found.") }
            }
        }
    }

    private fun listenForMessages() {
        taskRepository.getMessages(taskId).onEach { result ->
            if (result is Resource.Success) {
                _uiState.update { it.copy(messages = result.data ?: emptyList()) }
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null || text.isBlank()) {
            return
        }
        viewModelScope.launch {
            val message = Message(senderId = currentUserId, text = text)
            taskRepository.sendMessage(taskId, message)
            // Here you would also create a notification for the other user
        }
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun onMarkCompleteClicked() {
        _uiState.update { it.copy(showPostGigDialog = true) }
    }

    fun dismissPostGigDialog() {
        _uiState.update { it.copy(showPostGigDialog = false) }
    }

    fun submitReviewAndComplaint(rating: Int, comment: String, fileComplaint: Boolean) {
        // Here you would add the full backend logic for submitting review and complaint
        _uiState.update { it.copy(showPostGigDialog = false, reviewSubmitted = true) }
    }
}

class ChatViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(taskId, taskRepository, authRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

