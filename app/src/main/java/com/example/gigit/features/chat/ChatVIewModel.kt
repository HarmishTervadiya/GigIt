package com.example.gigit.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gigit.data.model.Message
import com.example.gigit.data.model.Task
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val messages: List<Message> = emptyList(),
    val error: String? = null
)

class ChatViewModel(
    private val taskId: String,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTaskDetails()
        listenForMessages()
    }

    private fun loadTaskDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = taskRepository.getTaskDetails(taskId)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, task = result.data) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
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
        }
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun markTaskAsCompleted() {
        viewModelScope.launch {
            taskRepository.markTaskAsCompleted(taskId)
            // In a real app, you might navigate or show a success message
        }
    }
}

class ChatViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(taskId, taskRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
