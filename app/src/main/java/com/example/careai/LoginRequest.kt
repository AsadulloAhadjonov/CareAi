package com.example.careai

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access: String,
    val refresh: String,
    val user: UserData,
    val message: String
)

data class UserData(
    val id: Int,
    val email: String,
    val username: String,
    val full_name: String,
    val mood_score: Int,
    val total_sessions: Int
)
