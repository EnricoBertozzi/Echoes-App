package com.nohana.echoes_app.service.network

import com.nohana.echoes_app.network.dto.AuthDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthNetworkService {

    @POST("/api/v1/auth/login")
    suspend fun login(@Body dto: AuthDTO.LoginRequest): Response<Void>;

    @POST("/api/v1/auth/mfa")
    suspend fun validate2fa(@Body dto: AuthDTO.TwoFactorRequest): Response<AuthDTO.TwoFactorResponse>

    @GET("/api/v1/auth/validate-token")
    suspend fun validateToken(@Header("Authorization") authHeader: String): Response<Unit>

    @GET("/api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") header: String): Response<Unit>
}