package com.nohana.echoes_app.classroom

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.nohana.echoes_app.network.dto.MAX_MATERIAL_BYTES
import com.nohana.echoes_app.network.dto.MaterialUpload
import com.nohana.echoes_app.network.dto.materialExtensions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal class AndroidClassroomFiles(context: Context) {
    private val resolver = context.contentResolver
    private val cacheDir = context.cacheDir

    suspend fun readUpload(uri: Uri): MaterialUpload = withContext(Dispatchers.IO) {
        val filename = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: throw IOException("Arquivo sem nome.")
        require(filename.substringAfterLast('.', "").lowercase() in materialExtensions) {
            "Formato de arquivo não permitido."
        }
        val input = resolver.openInputStream(uri) ?: throw IOException("Arquivo indisponível.")
        val bytes = input.use { source ->
            val data = ByteArrayOutputStream()
            val buffer = ByteArray(64 * 1024)
            var count = source.read(buffer)
            while (count != -1) {
                require(data.size() + count <= MAX_MATERIAL_BYTES) {
                    "O limite por arquivo é de 18 MB."
                }
                data.write(buffer, 0, count)
                count = source.read(buffer)
            }
            data.toByteArray()
        }
        MaterialUpload(filename, bytes)
    }

    suspend fun saveDownload(
        uri: Uri,
        api: ClassroomApi,
        classroomId: String,
        materialId: String,
    ) = withContext(Dispatchers.IO) {
        var temporary: File? = null
        var completed = false
        try {
            val file = File.createTempFile("echoes-material-", ".tmp", cacheDir)
            temporary = file
            file.outputStream().use { output ->
                api.download(classroomId, materialId) { buffer, count ->
                    output.write(buffer, 0, count)
                }
            }
            val destination = resolver.openOutputStream(uri, "w")
                ?: throw IOException("Destino indisponível.")
            destination.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            }
            completed = true
        } finally {
            temporary?.delete()
            if (!completed) withContext(NonCancellable) {
                try {
                    // O URI foi criado pelo seletor para este download.
                    DocumentsContract.deleteDocument(resolver, uri)
                } catch (_: Exception) {
                    // Alguns provedores não permitem excluir documentos por URI.
                }
            }
        }
    }
}
