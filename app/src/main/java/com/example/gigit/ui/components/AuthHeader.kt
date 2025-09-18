package com.example.gigit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Icon(
//            imageVector = Icons.Default.Task,
//            contentDescription = "GigIt Logo",
//            tint = MaterialTheme.colorScheme.primary
//        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("GigIt", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}