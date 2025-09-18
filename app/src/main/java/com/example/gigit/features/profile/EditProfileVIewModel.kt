package com.example.gigit.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gigit.data.model.User
import com.example.gigit.data.repository.AuthRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.AuthSource
import com.example.gigit.data.source.UserSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.example.gigit.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val user: User? = null,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val userProfile = userRepository.getUserProfile(userId)
                _uiState.update { it.copy(isLoading = false, user = userProfile) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not found.") }
            }
        }
    }

    fun saveProfile(username: String, upiId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                when (userRepository.updateUserProfile(userId, username, upiId)) {
                    is Resource.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = "Failed to save profile.") }
                    else -> _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }
}

object EditProfileViewModelFactory : ViewModelProvider.Factory {
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(authRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
