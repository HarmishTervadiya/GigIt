package com.example.gigit.features.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gigit.data.model.Task
import com.example.gigit.data.model.User
import com.example.gigit.data.repository.TaskRepository
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.TaskSource
import com.example.gigit.data.source.UserSource
import com.example.gigit.util.Resource
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.getValue

data class PaymentUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val poster: User? = null,
    val paymentSuccess: Boolean = false,
    val error: String? = null
)

class PaymentViewModel(
    private val taskId: String,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
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

    fun onPaymentSuccess(paymentId: String?) {
        viewModelScope.launch {
            taskRepository.updatePaymentStatus(taskId, "SUCCESS", paymentId)
            _uiState.update { it.copy(paymentSuccess = true) }
        }
    }

    fun onPaymentFailed() {
        viewModelScope.launch {
            taskRepository.updatePaymentStatus(taskId, "FAILED", null)
            _uiState.update { it.copy(error = "Payment failed. Please try again.") }
        }
    }
}

class PaymentViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    private val taskSource by lazy { TaskSource(Firebase.firestore) }
    private val taskRepository by lazy { TaskRepository(taskSource) }
    private val userSource by lazy { UserSource(Firebase.firestore) }
    private val userRepository by lazy { UserRepository(userSource) }


    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(taskId, taskRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

