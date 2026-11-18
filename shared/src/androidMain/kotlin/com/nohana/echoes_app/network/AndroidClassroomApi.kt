package com.nohana.echoes_app.classroom

import com.nohana.echoes_app.data.SessionTokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

fun createAndroidClassroomApi(
    baseUrl: String,
    okHttpClient: OkHttpClient,
    tokens: SessionTokenStore,
): ClassroomApi = ClassroomApi(
    HttpClient(OkHttp) {
        expectSuccess = false
        followRedirects = false
        engine {
            preconfigured = okHttpClient.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
    baseUrl,
    tokens,
)
