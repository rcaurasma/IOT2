package com.ev.iot2.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val name: String, val last_name: String, val email: String, val password: String)
data class RegisterResponse(val message: String, val user: User?)

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val token: String?, val user: User?)

data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(val message: String, val code: String?)

data class ResetPasswordRequest(val email: String, val code: String, val new_password: String)

data class User(val id: Int, val name: String, val last_name: String?, val email: String)

interface AuthService {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Map<String, String>>
}
