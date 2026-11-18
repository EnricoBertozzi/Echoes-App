package com.nohana.echoes_app.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class Classroom(
    val id: String,
    val title: String,
    val description: String? = null,
    val period: String,
    val archived: Boolean,
    val institutionId: String,
    val createdAt: String,
    val joinCode: String? = null,
)

@Serializable
data class ClassroomPage(
    val content: List<Classroom>,
    val totalPages: Int? = null,
    val page: PageMetadata? = null,
) {
    val pageCount: Int get() = page?.totalPages ?: totalPages ?: 0
}

@Serializable
data class PageMetadata(val totalPages: Int)

@Serializable
data class ClassroomInvite(val code: String, val expiresInSeconds: Long)

@Serializable
internal data class JoinClassroomRequest(val code: String)

@Serializable
data class ClassroomMaterial(
    val id: String,
    val title: String,
    val description: String? = null,
    val originalFileName: String,
    val contentType: String,
    val fileSize: Long,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val previewable: Boolean,
)

@Serializable
internal data class UpdateMaterialRequest(val title: String, val description: String)

data class MaterialUpload(val name: String, val bytes: ByteArray)

const val MAX_MATERIAL_BYTES = 18 * 1024 * 1024

val materialExtensions = setOf(
    "pdf", "png", "jpg", "jpeg", "gif", "webp", "docx", "pptx", "xlsx",
    "txt", "csv", "md",
)
