package com.ev.iot2.data.model

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class RecoveryCode(
    val id: Long = 0,
    val email: String,
    val code: String,
    val createdAt: Long = System.currentTimeMillis(),
    val used: Boolean = false
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

data class SensorData(
    val temperature: Float,
    val humidity: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class Developer(
    val name: String,
    val role: String,
    val email: String,
    val institution: String,
    val career: String
)
