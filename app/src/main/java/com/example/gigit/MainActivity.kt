package com.example.gigit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.gigit.navigation.AppNavigation
import com.example.gigit.ui.theme.GigItTheme
import com.example.gigit.util.PaymentResult
import com.example.gigit.util.PaymentResultBus
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val taskIdFromNotification = intent.getStringExtra("task_id_extra")

        setContent {
            GigItTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startTaskId = taskIdFromNotification)
                }
            }
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        GlobalScope.launch {
            PaymentResultBus.postResult(PaymentResult.Success(paymentId))
        }
    }

    override fun onPaymentError(code: Int, description: String?) {
        GlobalScope.launch {
            PaymentResultBus.postResult(PaymentResult.Error)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GigItTheme {
    }
}