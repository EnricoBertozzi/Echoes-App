package com.nohana.echoes_app.service.network

import com.nohana.echoes_app.network.dto.PasswordDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PasswordNetworkService {
    @POST("/api/v1/password/forgot")
    suspend fun forgotPassword(@Body dto: PasswordDTO.ForgotPasswordRequest): Response<Unit>

    @POST("/api/v1/password/reset")
    suspend fun resetPassword(@Body dto: PasswordDTO.ResetPasswordRequest): Response<Unit>

    @POST("/api/v1/password/validate")
    suspend fun validatePassword(
        @Header("Authorization") authHeader: String,
        @Body dto: PasswordDTO.ValidatePasswordRequest
    ): Response<PasswordDTO.ValidatePasswordResponse>

    @POST("/api/v1/password/change")
    suspend fun changePassword(
        @Header("Authorization") authHeader: String,
        @Body dto: PasswordDTO.ChangePasswordRequest
    ): Response<Unit>
}