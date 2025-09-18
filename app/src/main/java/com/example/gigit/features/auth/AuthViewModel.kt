package com.example.gigit.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gigit.data.repository.AuthRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.AuthSource
import com.example.gigit.data.source.UserSource
import com.example.gigit.util.Resource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val authSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private fun onAuthSuccess() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                val token = Firebase.messaging.token.await()
                if (userId != null && token.isNotEmpty()) {
                    userRepository.updateFcmToken(userId, token)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not update notification token.") }
            }
            // Trigger navigation after token is updated
            _uiState.update { it.copy(isLoading = false, authSuccess = true) }
        }
    }

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email, pass)) {
                is Resource.Success -> onAuthSuccess()
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> { /* Not used in this source */ }
            }
        }
    }

    fun signUpUser(email: String, pass: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signUp(email, pass, username)) {
                is Resource.Success -> onAuthSuccess()
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> { /* Not used in this source */ }
            }
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}

object AuthViewModelFactory : ViewModelProvider.Factory {
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

