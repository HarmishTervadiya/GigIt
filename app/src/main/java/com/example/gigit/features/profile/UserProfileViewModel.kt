package com.example.gigit.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Review
import com.example.gigit.data.model.User
import com.example.gigit.data.repository.TaskRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.TaskSource
import com.example.gigit.data.source.UserSource
import com.example.gigit.util.Resource
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val reviews: List<Review> = emptyList(),
    val error: String? = null
)

class UserProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userProfile = userRepository.getUserProfile(userId)
            _uiState.update { it.copy(user = userProfile) }

            taskRepository.getReviewsForUser(userId).onEach { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(isLoading = false, reviews = result.data ?: emptyList()) }
                } else if (result is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }.launchIn(viewModelScope)
        }
    }
}

class UserProfileViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(userId, userRepository, taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

