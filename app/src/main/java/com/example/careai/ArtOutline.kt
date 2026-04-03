package com.example.careai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa

data class ArtOutline(
    val id: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector, // Yoki ImageResource
    val name: String
)

val outlineList = listOf(
    ArtOutline(1, Icons.Default.Spa, "Lotus"),
    ArtOutline(2, Icons.Default.SelfImprovement, "Yoga"),
    ArtOutline(3, Icons.Default.FavoriteBorder, "Heart")
)