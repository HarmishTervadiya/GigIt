package com.example.gigit.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// A sealed class to represent the possible outcomes of a payment
sealed class PaymentResult {
    data class Success(val paymentId: String?) : PaymentResult()
    object Error : PaymentResult()
}

// A simple object to act as a communication bus between MainActivity and PaymentScreen
object PaymentResultBus {
    private val _resultFlow = MutableSharedFlow<PaymentResult>()
    val resultFlow = _resultFlow.asSharedFlow()

    suspend fun postResult(result: PaymentResult) {
        _resultFlow.emit(result)
    }

}
