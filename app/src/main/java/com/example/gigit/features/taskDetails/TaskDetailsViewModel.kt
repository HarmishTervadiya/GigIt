package com.example.gigit.features.task_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailsUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val error: String? = null,
    val isAccepting: Boolean = false,
    val acceptSuccess: Boolean = false
)

class TaskDetailsViewModel(
    private val taskId: String,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTaskDetails()
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

    fun acceptTask() {
        val taskerId = authRepository.getCurrentUserId()
        if (taskerId == null) {
            _uiState.update { it.copy(error = "You must be logged in to accept a task.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAccepting = true) }
            when (taskRepository.acceptTask(taskId, taskerId)) {
                is Resource.Success -> _uiState.update { it.copy(isAccepting = false, acceptSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isAccepting = false, error = "Could not accept task.") }
                else -> _uiState.update { it.copy(isAccepting = false) }
            }
        }
    }
}

class TaskDetailsViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailsViewModel(taskId, taskRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
