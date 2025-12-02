package com.ev.iot2.data.model

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

data class Developer(
    val name: String,
    val role: String,
    val email: String,
    val institution: String,
    val career: String
)
