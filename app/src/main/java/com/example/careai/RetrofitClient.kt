package com.example.careai

import CareAiApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 1. URL manzillarini ajratib olamiz
    private const val MY_BACKEND_URL = "https://epimeric-unmovable-toshiko.ngrok-free.dev/"
    private const val OPENAI_URL = "https://api.openai.com/"

    // 2. Umumiy OkHttpClient (Ikkala servis uchun ham bitta client yetarli)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // 3. Sizning mavjud backend instansiyangiz (Login/Register uchun)
    val instance: CareAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MY_BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CareAiApiService::class.java)
    }

    // 4. YANGI: OpenAI instansiyasi (STT, Chat, TTS uchun)
    // Bu yerda siz tashlagan OpenAiService interfeysidan foydalanamiz
    val openAiInstance: OpenAiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENAI_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiService::class.java)
    }
}