package com.example.careai

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

interface OpenAiService {
    // 1. STT: Ovozli faylni matnga aylantirish
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun speechToText(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody = "whisper-1".toRequestBody()
    ): TranscriptionResponse

    @POST("v1/chat/completions")
    suspend fun getChatResponse(
        @Header("Authorization") token: String,
        @Body request: ChatRequest2
    ): ChatResponse2

    @POST("v1/chat/completions")
    suspend fun getVisionResponse(
        @Header("Authorization") token: String,
        @Body request: VisionRequest
    ): ChatResponse2
    @POST("v1/audio/speech")
    @Streaming
    suspend fun textToSpeech(
        @Header("Authorization") token: String,
        @Body request: Map<String, String> // model, input, voice
    ): ResponseBody
}

data class ChatRequest2(val model: String = "gpt-4o", val messages: List<Message>)
data class Message(val role: String, val content: String)
data class ChatResponse2(val choices: List<Choice>)
data class Choice(val message: Message)
data class TranscriptionResponse(val text: String)