package com.example.gigit.features.payment

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gigit.util.PaymentResult
import com.example.gigit.util.PaymentResultBus
import com.razorpay.Checkout
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    taskId: String,
    navController: NavController
) {
    val viewModel: PaymentViewModel = viewModel(factory = PaymentViewModelFactory(taskId = taskId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalView.current.context as Activity

    // Listen for results from the MainActivity via the bus
    LaunchedEffect(Unit) {
        PaymentResultBus.resultFlow.collect { result ->
            when (result) {
                is PaymentResult.Success -> viewModel.onPaymentSuccess(result.paymentId)
                is PaymentResult.Error -> viewModel.onPaymentFailed()
            }
        }
    }

    // Preload Razorpay for faster checkout
    DisposableEffect(Unit) {
        Checkout.preload(activity.applicationContext)
        onDispose {}
    }

    // Navigate back on successful payment
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Complete Payment") }) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = uiState.error!!) }
            }
            uiState.task != null -> {
                val task = uiState.task!!
                val poster = uiState.poster!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("You are paying for:", style = MaterialTheme.typography.titleMedium)
                    Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Amount", style = MaterialTheme.typography.titleMedium)
                    Text("₹${task.rewardAmount}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            val checkout = Checkout()
                            // IMPORTANT: Replace with your actual Razorpay Test Key
                            checkout.setKeyID("rzp_test_")

                            try {
                                val options = JSONObject()
                                options.put("name", "GigIt")
                                options.put("description", "Payment for: ${task.title}")
                                options.put("theme.color", "#3A86FF")
                                options.put("currency", "INR")
                                options.put("amount", (task.rewardAmount * 100).toInt()) // Amount in paise

                                val prefill = JSONObject()
                                prefill.put("email", poster)
                                prefill.put("contact",poster.mobileNumber)
                                options.put("prefill",prefill)

                                checkout.open(activity, options)
                            } catch (e: Exception){
                                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Pay with Razorpay")
                    }
                }
            }
        }
    }
}

