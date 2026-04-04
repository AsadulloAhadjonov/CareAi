package com.example.careai

data class VisionMessage(
    val role: String,
    val content: List<Any> // TextContent yoki ImageContent bo'lishi mumkin
)