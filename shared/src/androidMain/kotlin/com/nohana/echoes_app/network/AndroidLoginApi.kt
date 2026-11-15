package com.nohana.echoes_app.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

fun createAndroidLoginApi(
    baseUrl: String,
    okHttpClient: OkHttpClient,
): LoginApi {
    val client = HttpClient(OkHttp) {
        expectSuccess = false
        engine {
            preconfigured = okHttpClient
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return LoginApi(client, baseUrl)
}