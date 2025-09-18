package com.example.gigit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.CurrencyRupee // Using Rupee for India
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddGigSheetContent(
    onPostGig: (title: String, description: String, location: String, amount: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title of the Bottom Sheet
        Text("Create a New Gig", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        // Task Title TextField
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = "Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Description TextField
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Location TextField
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Price TextField
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Reward Amount") },
            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = "Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Post Gig Button
        Button(
            onClick = { onPostGig(title, description, location, amount) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = title.isNotBlank() && description.isNotBlank() && amount.isNotBlank()
        ) {
            Text("Post Gig")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}