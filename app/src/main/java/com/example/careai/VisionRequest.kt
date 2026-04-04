package com.example.careai

data class VisionRequest(
    val model: String = "gpt-4o",
    val messages: List<VisionMessage>,
    val max_tokens: Int = 300
)