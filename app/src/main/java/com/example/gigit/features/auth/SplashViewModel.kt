package com.example.gigit.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gigit.data.repository.AuthRepository
import com.example.gigit.data.repository.UserPreferencesRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.AuthSource
import com.example.gigit.data.source.LocalUserPreferencesSource
import com.example.gigit.data.source.UserSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// import com.google.firebase.auth.FirebaseAuth // Uncomment when using Firebase
// import com.gigit.app.data.auth.AuthRepository
// import com.gigit.app.data.auth.AuthSource

// Sealed interface to represent the navigation destinations
sealed interface SplashDestination {
    object MainApp : SplashDestination
    object Onboarding : SplashDestination
    object None : SplashDestination

    object Auth : SplashDestination
}

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _navigateState = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val navigateState = _navigateState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1500) // Keep splash visible for a moment

            // Placeholder logic until AuthRepository is implemented
//            val isUserAuthenticated = false  // Change this for testing

//            if (isUserAuthenticated) {
//                _navigateState.value = SplashDestination.MainApp
//            } else if (prefsRepository.hasCompletedOnboarding()) {
//                // If onboarding is done but user is not logged in, go straight to Auth
//                _navigateState.value = SplashDestination.Auth
//            } else {
//                _navigateState.value = SplashDestination.Onboarding
//            }

            if (authRepository.isUserAuthenticated()) {
                updateFcmToken()
                _navigateState.value = SplashDestination.MainApp
            } else if (prefsRepository.hasCompletedOnboarding()) {
                // If onboarding is done but user is not logged in, go straight to Auth
                _navigateState.value = SplashDestination.Auth
            } else {
                _navigateState.value = SplashDestination.Onboarding
            }

        }
    }

    private fun updateFcmToken() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                val token = Firebase.messaging.token.await()
                if (userId != null && token.isNotEmpty()) {
                    userRepository.updateFcmToken(userId, token)
                }
            } catch (e: Exception) {
                // It's okay to fail silently here on splash.
                // The token will be updated on the next login or via the service.
            }
        }
    }
}

    // The Factory is now in the same file and creates the concrete classes
    class SplashViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        private val authSource by lazy { AuthSource(Firebase.auth) }
        private val userSource by lazy { UserSource(Firebase.firestore) }
        private val userRepository by lazy { UserRepository(userSource) }
        private val authRepository by lazy { AuthRepository(authSource, userRepository) }
        private val localPrefsSource by lazy { LocalUserPreferencesSource(context) }
        private val prefsRepository by lazy { UserPreferencesRepository(localPrefsSource) }

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                // Pass the new repository to the ViewModel
                return SplashViewModel(authRepository, prefsRepository, userRepository) as T // <-- UPDATED
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }