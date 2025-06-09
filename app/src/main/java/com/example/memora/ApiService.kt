package com.example.memoraapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @POST("api/chat/")
    suspend fun sendMessage(
        @Body chatRequest: ChatRequest,
        @Header("Authorization") authHeader: String
    ): Response<ChatResponse>

    @POST("api/login/")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("api/memory/")
    suspend fun saveMemory(
        @Body memoryRequest: MemoryRequest,
        @Header("Authorization") authHeader: String
    ): Response<Memory>

    @GET("api/memory/")
    suspend fun getMemories(
        @Header("Authorization") authHeader: String
    ): Response<List<Memory>>

    @GET("api/memory/{id}/")
    suspend fun getMemoryById(
        @Path("id") id: Long,
        @Header("Authorization") authHeader: String
    ): Response<Memory>
}
