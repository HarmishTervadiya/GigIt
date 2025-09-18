package com.example.gigit.features.taskDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Task
import com.example.gigit.data.model.User
import com.example.gigit.data.repository.AuthRepository
import com.example.gigit.data.repository.TaskRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.AuthSource
import com.example.gigit.data.source.TaskSource
import com.example.gigit.data.source.UserSource
import com.example.gigit.util.Constants
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
    val poster: User? = null, // <-- ADDED: To hold the poster's full profile
    val error: String? = null,
    val isAccepting: Boolean = false,
    val acceptSuccess: Boolean = false,
    val showUpiDialog: Boolean = false
)

class TaskDetailsViewModel(
    private val taskId: String,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
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
                is Resource.Success -> {
                    val task = result.data
                    if (task != null) {
                        // If task is found, fetch the poster's profile
                        val posterProfile = userRepository.getUserProfile(task.posterId)
                        _uiState.update { it.copy(isLoading = false, task = task, poster = posterProfile) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Task not found.") }
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading<*> -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun onAcceptClick() {
        val task = _uiState.value.task
        if (task?.rewardType == Constants.REWARD_TYPE_CASH) {
            viewModelScope.launch {
                val userId = authRepository.getCurrentUserId()!!
                val userProfile = userRepository.getUserProfile(userId)
                if (userProfile?.upiId.isNullOrBlank()) {
                    _uiState.update { it.copy(showUpiDialog = true) }
                } else {
                    acceptTask()
                }
            }
        } else {
            acceptTask()
        }
    }

    fun saveUpiAndAcceptTask(upiId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()!!
            userRepository.updateUpiId(userId, upiId)
            _uiState.update { it.copy(showUpiDialog = false) }
            acceptTask()
        }
    }

    fun dismissUpiDialog() {
        _uiState.update { it.copy(showUpiDialog = false) }
    }

    private fun acceptTask() {
        val taskerId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAccepting = true) }
            when (taskRepository.acceptTask(taskId, taskerId)) {
                is Resource.Success -> _uiState.update { it.copy(isAccepting = false, acceptSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isAccepting = false, error = "Could not accept task.") }
                is Resource.Loading<*> -> _uiState.update { it.copy(isLoading = true) }
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

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(TaskDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailsViewModel(taskId, taskRepository, authRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

