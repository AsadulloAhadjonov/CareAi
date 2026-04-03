package com.example.careai

import CareAiApiService
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://epimeric-unmovable-toshiko.ngrok-free.dev/"

    // Timeout vaqtlarini sozlash
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Serverga ulanish kutish vaqti
        .readTimeout(60, TimeUnit.SECONDS)    // Ma'lumotni o'qish kutish vaqti
        .writeTimeout(60, TimeUnit.SECONDS)   // Faylni yuborish kutish vaqti
        .build()

    val instance: CareAiApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Sozlangan clientni ulaymiz
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(CareAiApiService::class.java)
    }
}