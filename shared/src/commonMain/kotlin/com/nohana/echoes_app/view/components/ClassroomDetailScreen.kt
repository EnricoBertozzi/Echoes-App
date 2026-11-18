package com.nohana.echoes_app.classroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.network.dto.Classroom
import com.nohana.echoes_app.network.dto.ClassroomMaterial
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.ClassroomFeedback
import com.nohana.echoes_app.view.components.ClassroomMaterialCard

@Composable
fun ClassroomDetailScreen(
    classroom: Classroom,
    state: ClassroomState,
    teacher: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onGenerateInvite: () -> Unit,
    onShareInvite: (String) -> Unit,
    onDownload: (ClassroomMaterial) -> Unit,
    onEdit: (ClassroomMaterial) -> Unit,
    onReplace: (ClassroomMaterial) -> Unit,
    onDelete: (ClassroomMaterial) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDevices: () -> Unit = {},
) {
    val canManage = teacher && !classroom.archived

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onBack) {
                    Text("Minhas turmas", color = DarkBlue)
                }

                TextButton(onClick = onRefresh) {
                    Text("Atualizar", color = DarkBlue)
                }
            }
        }

        item {
            ClassroomFeedback(state.error, state.message)
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = classroom.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkBlue,
                )

                Text("Período: ${classroom.period}")

                classroom.description
                    ?.takeIf { it.isNotBlank() }
                    ?.let { Text(it) }

                if (classroom.archived) {
                    Text(
                        text = "Turma arquivada • somente consulta",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (canManage) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Convite para alunos",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkBlue,
                        )

                        val code = classroom.joinCode

                        if (code == null) {
                            Text("Gere um código para compartilhar com seus alunos.")

                            Button(
                                onClick = onGenerateInvite,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text("Gerar convite")
                            }
                        } else {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.headlineMedium,
                                color = DarkBlue,
                            )

                            Text("O servidor verifica a validade do código.")

                            OutlinedButton(
                                onClick = { onShareInvite(code) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DarkBlue,
                                ),
                            ) {
                                Text("Compartilhar convite")
                            }
                        }
                    }
                }
            }
        }

        if (teacher) {
            item {
                OutlinedButton(
                    onClick = onOpenDevices,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DarkBlue,
                    ),
                ) {
                    Text("Dispositivos da turma")
                }
            }
        }

        item {
            Text(
                text = "Materiais",
                style = MaterialTheme.typography.titleLarge,
                color = DarkBlue,
            )
        }

        if (state.materials.isEmpty() && state.error == null) {
            item {
                Text(
                    text = "Nenhum material publicado.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(
            items = state.materials,
            key = { it.id },
        ) { material ->
            ClassroomMaterialCard(
                material = material,
                canEdit = canManage,
                onDownload = { onDownload(material) },
                onEdit = { onEdit(material) },
                onReplace = { onReplace(material) },
                onDelete = { onDelete(material) },
            )
        }
    }
}