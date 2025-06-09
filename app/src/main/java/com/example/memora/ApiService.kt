package com.example.memoraapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/login/")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/chat/")
    suspend fun sendMessage(
        @Body chatRequest: ChatRequest,
        @Header("Authorization") authHeader: String
    ): Response<ChatResponse>
}
