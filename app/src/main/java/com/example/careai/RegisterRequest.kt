package com.example.careai

data class RegisterRequest(
    val email: String,
    val username: String, // Bu yerga full_name qiymatini beramiz
    val full_name: String,
    val password: String,
    val password2: String
)

// Serverdan qaytadigan javob (Muvaffaqiyatli bo'lsa)
data class RegResponse(
    val access_token: String?,
    val token_type: String?,
    val message: String?
)