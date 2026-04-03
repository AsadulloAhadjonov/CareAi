package com.example.careai

import androidx.compose.ui.graphics.Color

data class ColoredPath(
    val path: androidx.compose.ui.graphics.Path,
    val color: Color,
    val strokeWidth: Float = 40f
)