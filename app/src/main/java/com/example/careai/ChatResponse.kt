package com.example.careai

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("session_id")
    val sessionId: Int,

    @SerializedName("reply")
    val reply: String,

    @SerializedName("user_message")
    val userMessage: String,

    @SerializedName("audio_url")
    val audioUrl: String?,

    @SerializedName("current_stage")
    val currentStage: Int,

    @SerializedName("message_count")
    val messageCount: Int
)