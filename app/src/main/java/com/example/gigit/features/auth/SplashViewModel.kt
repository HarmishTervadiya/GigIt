package com.example.gigit.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// import com.google.firebase.auth.FirebaseAuth // Uncomment when using Firebase
// import com.gigit.app.data.auth.AuthRepository
// import com.gigit.app.data.auth.AuthSource

// Sealed interface to represent the navigation destinations
sealed interface SplashDestination {
    object MainApp : SplashDestination
    object Onboarding : SplashDestination
    object None : SplashDestination
}

class SplashViewModel(
    // private val authRepository: AuthRepository   // Uncomment later when repo is ready
) : ViewModel() {

    private val _navigateState = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val navigateState = _navigateState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1500) // Keep splash visible for a moment

            // Placeholder logic until AuthRepository is implemented
//            val isUserAuthenticated = false  // Change this for testing

//            if (isUserAuthenticated) {
                _navigateState.value = SplashDestination.MainApp
//            } else {
//                _navigateState.value = SplashDestination.Onboarding
//            }

            // ---- Actual logic when you implement AuthRepository ----
            /*
            if (authRepository.isUserAuthenticated()) {
                _navigateState.value = SplashDestination.MainApp
            } else {
                _navigateState.value = SplashDestination.Onboarding
            }
            */
        }
    }
}

// The Factory is now in the same file and creates the concrete classes
object SplashViewModelFactory : ViewModelProvider.Factory {
    // Manually create the dependency chain: Source -> Repository
    // private val authSource by lazy { AuthSource(FirebaseAuth.getInstance()) }
    // private val authRepository by lazy { AuthRepository(authSource) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(
                // authRepository   // Uncomment when ready
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
