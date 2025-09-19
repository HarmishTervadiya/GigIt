package com.example.gigit.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Message
import com.example.gigit.data.model.Review
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
    val showReviewSheet: Boolean = false,
    val reviewSubmitted: Boolean = false,
    val navigateToPayment: Boolean = false
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
        listenForTaskUpdates()
    }

    private fun listenForTaskUpdates() {
        taskRepository.getTaskDetailsFlow(taskId).onEach { result ->
            if (result is Resource.Success && result.data != null) {
                val task = result.data
                if (_uiState.value.currentUser == null) {
                    loadUserProfiles(task) // Fetch user profiles only on the first load
                }
                _uiState.update { it.copy(isLoading = false, task = task) }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadUserProfiles(task: Task) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            val currentUserProfile = userRepository.getUserProfile(currentUserId)
            val otherUserId = if (currentUserId == task.posterId) task.taskerId else task.posterId
            val otherUserProfile = otherUserId?.let { userRepository.getUserProfile(it) }
            _uiState.update { it.copy(currentUser = currentUserProfile, otherUser = otherUserProfile) }
        }
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

    fun onLeaveReviewClicked() {
        _uiState.update { it.copy(showReviewSheet = true) }
    }

    fun dismissReviewSheet() {
        _uiState.update { it.copy(showReviewSheet = false) }
    }

    fun submitReview(rating: Int, comment: String, paymentSuccess: Boolean?) {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            val currentUser = _uiState.value.currentUser ?: return@launch
            val otherUser = _uiState.value.otherUser ?: return@launch

            val review = Review(
                taskId = taskId,
                reviewerId = currentUser.uid,
                revieweeId = otherUser.uid,
                rating = rating,
                comment = comment,
                paymentCompletedSuccessfully = paymentSuccess
            )
            taskRepository.submitReview(review)
            // Here you would also create notifications for the review
            _uiState.update { it.copy(showReviewSheet = false, reviewSubmitted = true) }
        }
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
        viewModelScope.launch {
            taskRepository.markTaskAsCompleted(taskId)
        }
    }

    fun onNavigateToPaymentHandled() {
        _uiState.update { it.copy(navigateToPayment = false) }
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

