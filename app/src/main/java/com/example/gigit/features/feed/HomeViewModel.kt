package com.example.gigit.features.feed

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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository // <-- ADDED
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchOpenTasks()
    }

    private fun fetchOpenTasks() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _uiState.update { it.copy(isLoading = false, error = "You are not logged in.") }
            return
        }

        // Pass the user's ID to the repository to filter the feed
        taskRepository.getOpenTasks(currentUserId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, tasks = result.data ?: emptyList())
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
}

object HomeViewModelFactory : ViewModelProvider.Factory {
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }

    // Add the full dependency chain for AuthRepository
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pass both repositories to the ViewModel
            return HomeViewModel(taskRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

