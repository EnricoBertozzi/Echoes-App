package com.nohana.echoes_app.classroom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.nohana.echoes_app.network.dto.Classroom
import com.nohana.echoes_app.network.dto.ClassroomMaterial
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.ClassroomDeleteMaterialDialog
import com.nohana.echoes_app.view.components.ClassroomJoinDialog
import com.nohana.echoes_app.view.components.ClassroomMaterialDialog
import com.nohana.echoes_app.view.components.LoadingScreen

@Composable
fun ClassroomScreen(
    state: ClassroomState,
    teacher: Boolean,
    addMaterialRequested: Boolean,
    onDismissAddMaterial: () -> Unit,
    onSearch: (String) -> Unit,
    onPage: (Int) -> Unit,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onJoin: (String) -> Unit,
    onGenerateInvite: () -> Unit,
    onShareInvite: (String) -> Unit,
    onAddMaterial: (String, String) -> Unit,
    onUpdateMaterial: (String, String, String) -> Unit,
    onReplaceFile: (ClassroomMaterial) -> Unit,
    onDeleteMaterial: (String) -> Unit,
    onDownload: (Classroom, ClassroomMaterial) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDevices: (Classroom) -> Unit = {},
) {
    val classroom = state.detail

    var joinOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var editId by rememberSaveable(classroom?.id) {
        mutableStateOf<String?>(null)
    }

    var deleteId by rememberSaveable(classroom?.id) {
        mutableStateOf<String?>(null)
    }

    // Ajusta a cor do indicador de loading e dos controles desta área.
    val colors = MaterialTheme.colorScheme.copy(
        primary = DarkBlue,
    )

    MaterialTheme(colorScheme = colors) {
        Box(modifier = modifier.fillMaxSize()) {
            if (state.busy) {
                LoadingScreen()
            } else if (classroom == null) {
                ClassroomListScreen(
                    state = state,
                    teacher = teacher,
                    onSearch = onSearch,
                    onJoin = { joinOpen = true },
                    onOpen = onOpen,
                    onPage = onPage,
                )
            } else {
                ClassroomDetailScreen(
                    classroom = classroom,
                    state = state,
                    teacher = teacher,
                    onBack = onBack,
                    onRefresh = onRefresh,
                    onGenerateInvite = onGenerateInvite,
                    onShareInvite = onShareInvite,
                    onDownload = { onDownload(classroom, it) },
                    onEdit = { editId = it.id },
                    onReplace = onReplaceFile,
                    onDelete = { deleteId = it.id },
                    onOpenDevices = {
                        if (teacher) {
                            onOpenDevices(classroom)
                        }
                    },
                )
            }
        }

        if (!state.busy) {
            if (joinOpen && !teacher && classroom == null) {
                ClassroomJoinDialog(
                    onDismiss = { joinOpen = false },
                    onConfirm = { code ->
                        joinOpen = false
                        onJoin(code)
                    },
                )
            }

            val canManage = teacher && classroom?.archived == false

            if (addMaterialRequested && canManage) {
                ClassroomMaterialDialog(
                    onDismiss = onDismissAddMaterial,
                    onConfirm = { title, description ->
                        onDismissAddMaterial()
                        onAddMaterial(title, description)
                    },
                )
            }

            if (canManage) {
                state.materials
                    .firstOrNull { it.id == editId }
                    ?.let { material ->
                        ClassroomMaterialDialog(
                            material = material,
                            onDismiss = { editId = null },
                            onConfirm = { title, description ->
                                editId = null
                                onUpdateMaterial(
                                    material.id,
                                    title,
                                    description,
                                )
                            },
                        )
                    }

                state.materials
                    .firstOrNull { it.id == deleteId }
                    ?.let { material ->
                        ClassroomDeleteMaterialDialog(
                            material = material,
                            onDismiss = { deleteId = null },
                            onConfirm = {
                                deleteId = null
                                onDeleteMaterial(material.id)
                            },
                        )
                    }
            }
        }
    }
}