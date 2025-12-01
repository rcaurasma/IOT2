package com.ev.iot2.data.api

import com.ev.iot2.BuildConfig
import com.ev.iot2.data.model.ApiResponse
import com.ev.iot2.data.model.LoginRequest
import com.ev.iot2.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse>

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>
}

object RetrofitClient {
    private var baseUrl = BuildConfig.API_BASE_URL
    
    private var retrofit: Retrofit? = null
    
    fun getApiService(): ApiService {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
    
    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
        retrofit = null
    }
}
