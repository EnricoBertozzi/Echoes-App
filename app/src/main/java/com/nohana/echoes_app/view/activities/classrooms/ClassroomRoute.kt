package com.nohana.echoes_app.view.activities.classrooms

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.nohana.echoes_app.classroom.AndroidClassroomFiles
import com.nohana.echoes_app.classroom.ClassroomScreen
import com.nohana.echoes_app.classroom.ClassroomState
import com.nohana.echoes_app.classroom.ClassroomViewModel
import com.nohana.echoes_app.network.dto.Classroom

@Composable
fun ClassroomRoute(
    viewModel: ClassroomViewModel,
    state: ClassroomState,
    teacher: Boolean,
    addMaterialRequested: Boolean,
    onDismissAddMaterial: () -> Unit,
    onOpenDevices: (Classroom) -> Unit = {},
) {
    val context = LocalContext.current

    val files = remember(context.applicationContext) {
        AndroidClassroomFiles(context.applicationContext)
    }

    var uploadClassroomId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var uploadMaterialId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var uploadTitle by rememberSaveable {
        mutableStateOf("")
    }

    var uploadDescription by rememberSaveable {
        mutableStateOf("")
    }

    var downloadClassroomId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var downloadMaterialId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val classroomId = uploadClassroomId
        val materialId = uploadMaterialId
        val title = uploadTitle
        val description = uploadDescription

        uploadClassroomId = null
        uploadMaterialId = null

        if (uri != null && classroomId != null) {
            if (viewModel.state.value.detail?.id != classroomId) {
                viewModel.showError(
                    "Abra a turma e selecione o arquivo novamente."
                )
            } else if (materialId == null) {
                viewModel.addMaterial(title, description) {
                    files.readUpload(uri)
                }
            } else {
                viewModel.replaceFile(materialId) {
                    files.readUpload(uri)
                }
            }
        }
    }

    val saveFile = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val classroomId = downloadClassroomId
        val materialId = downloadMaterialId

        downloadClassroomId = null
        downloadMaterialId = null

        val uri = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data
        } else {
            null
        }

        if (uri != null && classroomId != null && materialId != null) {
            viewModel.saveMaterial(classroomId, materialId) { api, id, material ->
                files.saveDownload(uri, api, id, material)
            }
        }
    }

    BackHandler(
        enabled = state.detail != null || state.busy,
    ) {
        if (!state.busy) {
            viewModel.back()
        }
    }

    ClassroomScreen(
        state = state,
        teacher = teacher,
        addMaterialRequested = addMaterialRequested,
        onDismissAddMaterial = onDismissAddMaterial,
        onSearch = { viewModel.list(search = it) },
        onPage = { viewModel.list(page = it) },
        onOpen = { viewModel.open(it) },
        onBack = { viewModel.back() },
        onRefresh = {
            state.detail?.let { viewModel.open(it.id) }
        },
        onJoin = { viewModel.join(it) },
        onGenerateInvite = { viewModel.generateInvite() },
        onShareInvite = { code ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Código de convite do Echoes: $code",
                )
            }

            context.startActivity(
                Intent.createChooser(intent, "Compartilhar convite")
            )
        },
        onAddMaterial = { title, description ->
            uploadClassroomId = state.detail?.id
            uploadMaterialId = null
            uploadTitle = title
            uploadDescription = description

            pickFile.launch(arrayOf("*/*"))
        },
        onUpdateMaterial = { id, title, description ->
            viewModel.updateMaterial(id, title, description)
        },
        onReplaceFile = { material ->
            uploadClassroomId = state.detail?.id
            uploadMaterialId = material.id

            pickFile.launch(arrayOf("*/*"))
        },
        onDeleteMaterial = { viewModel.deleteMaterial(it) },
        onDownload = { classroom, material ->
            downloadClassroomId = classroom.id
            downloadMaterialId = material.id

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)

                type = material.contentType
                    .substringBefore(';')
                    .trim()
                    .ifBlank { "application/octet-stream" }

                putExtra(
                    Intent.EXTRA_TITLE,
                    material.originalFileName,
                )
            }

            saveFile.launch(intent)
        },
        onOpenDevices = onOpenDevices,
    )
}