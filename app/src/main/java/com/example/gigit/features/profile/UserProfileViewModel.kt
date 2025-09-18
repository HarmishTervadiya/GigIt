package com.example.gigit.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gigit.data.model.User
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.UserSource
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null
)

class UserProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userProfile = userRepository.getUserProfile(userId)
            if (userProfile != null) {
                _uiState.update { it.copy(isLoading = false, user = userProfile) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Could not load profile.") }
            }
        }
    }
}

class UserProfileViewModelFactory(
    private val userId: String
) : ViewModelProvider.Factory {
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(userId, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
