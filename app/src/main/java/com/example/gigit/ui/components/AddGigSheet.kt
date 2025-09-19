package com.example.gigit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gigit.ui.theme.BluePrimary
import com.example.gigit.ui.theme.BorderGray
import com.example.gigit.ui.theme.TextGray
import com.example.gigit.ui.theme.White
import com.example.gigit.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGigSheetContent(
    onPostGig: (title: String, description: String, category: String, location: String, rewardType: String, amount: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var rewardType by remember { mutableStateOf(Constants.REWARD_TYPE_CASH) }

    val categories = listOf("Food", "Stationary", "Transport", "Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Custom colors to match your HomeScreen design
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BluePrimary,
        unfocusedBorderColor = BorderGray,
        focusedContainerColor = White,
        unfocusedContainerColor = White,
        focusedLeadingIconColor = BluePrimary,
        unfocusedLeadingIconColor = TextGray
    )

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visual handle for sheet
        Surface(
            modifier = Modifier.size(width = 32.dp, height = 3.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = BorderGray
        ) {}

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Create a New Gig",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Title Field
        Text(
            "Task Title",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Enter task title") },
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = "Title", modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Description Field
        Text(
            "Description",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Describe your task in detail") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 4,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Category Field
        Text(
            "Category",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = isCategoryMenuExpanded,
            onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select category") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Category", modifier = Modifier.size(20.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().height(56.dp),
                colors = textFieldColors
            )
            ExposedDropdownMenu(
                expanded = isCategoryMenuExpanded,
                onDismissRequest = { isCategoryMenuExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            isCategoryMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Location Field
        Text(
            "Location",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            placeholder = { Text("Enter location") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Reward Type Selection
        Text(
            "Reward Type",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            SegmentedButton(
                selected = rewardType == Constants.REWARD_TYPE_CASH,
                onClick = { rewardType = Constants.REWARD_TYPE_CASH },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = BluePrimary,
                    activeContentColor = White
                )
            ) {
                Text("Cash", style = MaterialTheme.typography.bodyMedium)
            }
            SegmentedButton(
                selected = rewardType == Constants.REWARD_TYPE_FAVOR,
                onClick = { rewardType = Constants.REWARD_TYPE_FAVOR },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = BluePrimary,
                    activeContentColor = White
                )
            ) {
                Text("Favor", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Amount Field
        Text(
            if (rewardType == Constants.REWARD_TYPE_CASH) "Amount (₹)" else "Favor Details",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            placeholder = {
                Text(if (rewardType == Constants.REWARD_TYPE_CASH) "Enter amount in rupees" else "Describe the favor")
            },
            leadingIcon = {
                Icon(
                    if (rewardType == Constants.REWARD_TYPE_CASH) Icons.Default.CurrencyRupee else Icons.Default.Handshake,
                    contentDescription = "Amount",
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            keyboardOptions = if (rewardType == Constants.REWARD_TYPE_CASH) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
                KeyboardOptions.Default
            },
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onPostGig(title, description, selectedCategory, location, rewardType, amount)
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            enabled = title.isNotBlank() &&
                    description.isNotBlank() &&
                    location.isNotBlank() &&
                    (rewardType == Constants.REWARD_TYPE_FAVOR || amount.isNotBlank()),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Post Gig", style = MaterialTheme.typography.labelLarge)
        }

        // Bottom padding for keyboard avoidance
        Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp))
    }
}