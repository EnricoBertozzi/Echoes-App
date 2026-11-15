package com.nohana.echoes_app.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException
import kotlinx.serialization.Serializable

@Serializable
private data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
private data class MfaRequest(val email: String, val code: String)

@Serializable
private data class MfaResponse(val token: String)

sealed interface MfaResult {
    data class Confirmed(val token: String) : MfaResult
    data object InvalidCode : MfaResult
    data object Failed : MfaResult
}

@Serializable
data class UserProfile(
    val name: String,
    val email: String,
    val role: String,
    val institutionId: String?,
)

sealed interface CurrentUserResult {
    data class Found(val user: UserProfile) : CurrentUserResult
    data object Unauthorized : CurrentUserResult
    data object Failed : CurrentUserResult
}


class LoginApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun login(email: String, password: String): Boolean {
        val response = client.post("${baseUrl}/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }

        return response.status.isSuccess()
    }

    suspend fun confirmMfa(email: String, code: String): MfaResult {
        val response = client.post("${baseUrl}/api/v1/auth/mfa") {
            contentType(ContentType.Application.Json)
            setBody(MfaRequest(email, code))
        }

        return when (response.status.value) {
            200 -> {
                try {
                    val token = response.body<MfaResponse>().token

                    if (token.isBlank()) {
                        MfaResult.Failed
                    } else {
                        MfaResult.Confirmed(token)
                    }
                } catch (e: JsonConvertException) {
                    MfaResult.Failed
                }
            }

            400, 401, 403 -> MfaResult.InvalidCode
            else -> MfaResult.Failed
        }
    }

    suspend fun getCurrentUser(token: String): CurrentUserResult {
        val response = client.get("${baseUrl}/api/v1/users/me") {
            bearerAuth(token)
        }

        return when (response.status.value) {
            200 -> {
                try {
                    val user = response.body<UserProfile>()
                    if (user.role.isBlank())
                        CurrentUserResult.Failed
                    else
                        CurrentUserResult.Found(user)
                } catch (e: JsonConvertException) {
                    CurrentUserResult.Failed
                }
            }

            401 -> CurrentUserResult.Unauthorized
            else -> CurrentUserResult.Failed
        }
    }

    suspend fun logout(token: String): Boolean {
        val response = client.post("${baseUrl}/api/v1/auth/logout") {
            bearerAuth(token)
        }

        return response.status.isSuccess() || response.status.value == 401
    }
}
