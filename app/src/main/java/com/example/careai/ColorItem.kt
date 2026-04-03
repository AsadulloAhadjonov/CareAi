package com.example.careai

import androidx.compose.ui.graphics.Color

data class ColorItem(
    val id: Int,
    val color: Color,
    val name: String
)

val diagnosticColors = listOf(
    ColorItem(1, Color(0xFFE53935), "Qizil"),
    ColorItem(2, Color(0xFF1E88E5), "Ko'k"),
    ColorItem(3, Color(0xFF43A047), "Yashil"),
    ColorItem(4, Color(0xFFFFEB3B), "Sariq"),
    ColorItem(5, Color(0xFF8E24AA), "Binafsha"),
    ColorItem(6, Color(0xFFFB8C00), "Olovrang"),
    ColorItem(7, Color(0xFF00ACC1), "Feruza"),
    ColorItem(8, Color(0xFF5D4037), "Jigarrang"),
    ColorItem(9, Color(0xFF000000), "Qora"),
    ColorItem(10, Color(0xFF757575), "Kulrang"),
    ColorItem(11, Color(0xFFD81B60), "To'q pushti"),
    ColorItem(12, Color(0xFF3949AB), "To'q ko'k")
)
