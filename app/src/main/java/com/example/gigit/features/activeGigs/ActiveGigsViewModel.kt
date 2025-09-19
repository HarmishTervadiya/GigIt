package com.example.gigit.features.activeGigs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Task
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class ActiveGigsUiState(
    val isLoading: Boolean = true,
    val postedGigs: List<Task> = emptyList(),
    val acceptedGigs: List<Task> = emptyList(),
    val selectedTabIndex: Int = 0,
    val error: String? = null
)

class ActiveGigsViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveGigsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchActiveGigs()
    }

    private fun fetchActiveGigs() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
            return
        }
        taskRepository.getActiveGigs(userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val allGigs = result.data ?: emptyList()

//                    // An active gig is one that is NOT fully completed AND paid for.
//                    val trulyActiveGigs = allGigs.filterNot { task ->
//                        task.status == Constants.TASK_STATUS_COMPLETED && task.paymentStatus == "SUCCESS"
//                    }
//
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            postedGigs = trulyActiveGigs.filter { task -> task.posterId == userId },
//                            acceptedGigs = trulyActiveGigs.filter { task -> task.taskerId == userId }
//                        )
//                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            postedGigs = allGigs.filter { task -> task.posterId == userId },
                            acceptedGigs = allGigs.filter { task -> task.taskerId == userId }
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }.launchIn(viewModelScope)
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }
}

object ActiveGigsViewModelFactory : ViewModelProvider.Factory {
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }
    private val authSource by lazy { AuthSource(Firebase.auth) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }
    private val authRepository by lazy { AuthRepository(authSource, userRepository) }

    // --- THIS IS THE CORRECTED FUNCTION ---
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ActiveGigsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActiveGigsViewModel(taskRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

