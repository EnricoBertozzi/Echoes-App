package com.nohana.echoes_app.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.classroom.ClassroomState
import com.nohana.echoes_app.network.dto.Classroom
import com.nohana.echoes_app.network.dto.ClassroomMaterial
import com.nohana.echoes_app.ui.theme.DarkBlue

@Composable
fun ClassroomAddMaterialButton(
    state: ClassroomState,
    teacher: Boolean,
    onClick: () -> Unit,
) {
    if (!teacher || state.detail == null ||
        state.detail.archived || state.busy
    ) {
        return
    }

    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = DarkBlue,
        contentColor = Color.White,
        icon = {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = { Text("Adicionar material") },
    )
}

@Composable
fun ClassroomCard(
    classroom: Classroom,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = classroom.title,
                style = MaterialTheme.typography.titleMedium,
                color = DarkBlue,
            )

            Text(
                text = "Período: ${classroom.period}",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (classroom.archived) {
                Text(
                    text = "Arquivada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ClassroomMaterialCard(
    material: ClassroomMaterial,
    canEdit: Boolean,
    onDownload: () -> Unit,
    onEdit: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = material.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkBlue,
                )

                if (canEdit) {
                    Box {
                        TextButton(onClick = { menuOpen = true }) {
                            Text("Opções", color = DarkBlue)
                        }

                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar informações") },
                                onClick = {
                                    menuOpen = false
                                    onEdit()
                                },
                            )

                            DropdownMenuItem(
                                text = { Text("Substituir arquivo") },
                                onClick = {
                                    menuOpen = false
                                    onReplace()
                                },
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Remover material",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    menuOpen = false
                                    onDelete()
                                },
                            )
                        }
                    }
                }
            }

            material.description
                ?.takeIf { it.isNotBlank() }
                ?.let { Text(it) }

            Text(
                text = material.originalFileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "Publicado por ${material.authorName}",
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedButton(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkBlue,
                ),
            ) {
                Text("Salvar arquivo")
            }
        }
    }
}

@Composable
fun ClassroomFeedback(
    error: String?,
    message: String?,
) {
    if (error != null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    } else if (message != null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkBlue.copy(alpha = 0.08f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = DarkBlue,
            )
        }
    }
}

