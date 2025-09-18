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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
            ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle to indicate a draggable sheet
//        Surface(
//            modifier = Modifier.size(width = 40.dp, height = 4.dp),
//            shape = MaterialTheme.shapes.extraLarge,
//            color = BorderGray
//        ) {}

        Spacer(modifier = Modifier.height(16.dp))

        Text("Create a New Gig", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = "Title", modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = isCategoryMenuExpanded,
            onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Category", modifier = Modifier.size(20.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().height(52.dp),
                colors = textFieldColors
            )
            ExposedDropdownMenu(
                expanded = isCategoryMenuExpanded,
                onDismissRequest = { isCategoryMenuExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(text = { Text(category) }, onClick = {
                        selectedCategory = category
                        isCategoryMenuExpanded = false
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            SegmentedButton(
                selected = rewardType == Constants.REWARD_TYPE_CASH,
                onClick = { rewardType = Constants.REWARD_TYPE_CASH },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = BluePrimary, activeContentColor = White)
            ) { Text("Cash") }
            SegmentedButton(
                selected = rewardType == Constants.REWARD_TYPE_FAVOR,
                onClick = { rewardType = Constants.REWARD_TYPE_FAVOR },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = BluePrimary, activeContentColor = White)
            ) { Text("Favor") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Reward Amount") },
            enabled = rewardType == Constants.REWARD_TYPE_CASH,
            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = "Amount", modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onPostGig(title, description, selectedCategory, location, rewardType, amount) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = title.isNotBlank() && description.isNotBlank() && (rewardType == Constants.REWARD_TYPE_FAVOR || amount.isNotBlank())
        ) {
            Text("Post Gig")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

