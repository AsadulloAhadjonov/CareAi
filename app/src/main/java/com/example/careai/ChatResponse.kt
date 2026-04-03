package com.example.careai

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("reply")
    val reply: String,

    @SerializedName("message")
    val userMessage: String? = null
)