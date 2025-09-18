package com.example.gigit.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Review
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

data class MyProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val reviews: List<Review> = emptyList(),
    val error: String? = null
)

class MyProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Fetch profile and reviews in parallel
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

    fun signOut() {
        authRepository.signOut()
    }
}

object MyProfileViewModelFactory : ViewModelProvider.Factory {
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyProfileViewModel(authRepository, userRepository, taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

