package com.example.gigit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),      // For chips and smaller elements
    medium = RoundedCornerShape(12.dp),     // For main cards and text fields
    large = RoundedCornerShape(24.dp)       // For buttons and bottom sheets
)