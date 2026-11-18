package com.nohana.echoes_app.classroom

import com.nohana.echoes_app.data.SessionTokenStore
import com.nohana.echoes_app.network.dto.Classroom
import com.nohana.echoes_app.network.dto.ClassroomInvite
import com.nohana.echoes_app.network.dto.ClassroomMaterial
import com.nohana.echoes_app.network.dto.ClassroomPage
import com.nohana.echoes_app.network.dto.JoinClassroomRequest
import com.nohana.echoes_app.network.dto.MAX_MATERIAL_BYTES
import com.nohana.echoes_app.network.dto.MaterialUpload
import com.nohana.echoes_app.network.dto.UpdateMaterialRequest
import com.nohana.echoes_app.network.dto.materialExtensions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ClassroomApiException(val status: Int, message: String) : Exception(message)

@Serializable
private data class ErrorResponse(val message: String? = null)

class ClassroomApi internal constructor(
    private val client: HttpClient,
    baseUrl: String,
    private val tokens: SessionTokenStore,
) {
    private val base = "${baseUrl.trimEnd('/')}/api/v1/classrooms"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun list(name: String, page: Int): ClassroomPage =
        request(HttpMethod.Get) {
            parameter("name", name)
            parameter("page", page)
            parameter("size", 20)
            parameter("sort", "title,asc")
        }.body()

    suspend fun detail(id: String): Classroom = request(HttpMethod.Get, "/$id").body()

    suspend fun invite(id: String): ClassroomInvite =
        request(HttpMethod.Post, "/$id/code").body()

    suspend fun join(code: String): Classroom =
        request(HttpMethod.Post, "/join") {
            contentType(ContentType.Application.Json)
            setBody(JoinClassroomRequest(code))
        }.body()

    suspend fun materials(id: String): List<ClassroomMaterial> =
        request(HttpMethod.Get, "/$id/materials").body()

    suspend fun addMaterial(id: String, title: String, description: String, file: MaterialUpload) {
        request(HttpMethod.Post, "/$id/materials") {
            setBody(multipart(file, title, description))
        }
    }

    suspend fun updateMaterial(id: String, materialId: String, title: String, description: String) {
        request(HttpMethod.Patch, "/$id/materials/$materialId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateMaterialRequest(title, description))
        }
    }

    suspend fun replaceFile(id: String, materialId: String, file: MaterialUpload) {
        request(HttpMethod.Patch, "/$id/materials/$materialId/file") {
            setBody(multipart(file))
        }
    }

    suspend fun deleteMaterial(id: String, materialId: String) {
        request(HttpMethod.Delete, "/$id/materials/$materialId")
    }

    suspend fun download(id: String, materialId: String, write: (ByteArray, Int) -> Unit) {
        client.prepareGet("$base/$id/materials/$materialId/download") {
            authenticate()
        }.execute { response ->
            response.requireSuccess()
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(64 * 1024)
            var total = 0
            while (true) {
                val count = channel.readAvailable(buffer)
                if (count == -1) break
                total += count
                check(total <= MAX_MATERIAL_BYTES) { "Arquivo maior que 18 MB." }
                write(buffer, count)
            }
            check(total > 0) { "O servidor retornou um arquivo vazio." }
        }
    }

    fun close() = client.close()

    private suspend fun request(
        method: HttpMethod,
        path: String = "",
        configure: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse = client.request("$base$path") {
        this.method = method
        authenticate()
        configure()
    }.also { it.requireSuccess() }

    private fun HttpRequestBuilder.authenticate() {
        val token = tokens.getToken()?.takeIf { it.isNotBlank() }
            ?: throw ClassroomApiException(401, "Entre novamente para acessar suas turmas.")
        bearerAuth(token)
    }

    private suspend fun HttpResponse.requireSuccess() {
        if (status.isSuccess()) return
        val text = bodyAsText()
        val message = try {
            json.decodeFromString<ErrorResponse>(text).message
        } catch (_: IllegalArgumentException) {
            null
        }
        val fallback = when (status.value) {
            401 -> "Sessão expirada. Entre novamente."
            403 -> "Você não tem acesso a esta operação ou o convite é inválido."
            404 -> "Turma ou material não encontrado."
            409 -> "A operação conflita com o estado atual da turma."
            413 -> "Arquivo maior que o limite de upload."
            else -> "A operação falhou (HTTP ${status.value})."
        }
        throw ClassroomApiException(status.value, message?.takeIf { it.isNotBlank() } ?: fallback)
    }

    private fun multipart(
        file: MaterialUpload,
        title: String? = null,
        description: String? = null,
    ): MultiPartFormDataContent {
        val filename = file.name
        require(filename.isNotBlank() && filename.length <= 255 && ".." !in filename &&
                filename.none { it == '/' || it == '\\' || it == '"' || it.code < 32 || it.code == 127 }
        ) { "Nome do arquivo inválido." }
        require(filename.substringAfterLast('.', "").lowercase() in materialExtensions) {
            "Formato de arquivo não permitido."
        }
        require(file.bytes.size in 1..MAX_MATERIAL_BYTES) { "Selecione um arquivo de até 18 MB." }
        return MultiPartFormDataContent(formData {
            if (title != null) append("title", title)
            if (description != null) append("description", description)
            append("file", file.bytes, Headers.build {
                append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
            })
        })
    }
}
