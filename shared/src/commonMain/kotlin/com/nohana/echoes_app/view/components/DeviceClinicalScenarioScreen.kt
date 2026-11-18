package com.nohana.echoes_app.classroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.ClassroomFeedback
import com.nohana.echoes_app.viewmodel.DeviceScenarioState

@Composable
fun DeviceClinicalScenarioScreen(
    state: DeviceScenarioState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenPoint: (String) -> Unit,
) {
    val device = state.device ?: return
    val config = state.config

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onBack) {
                    Text("Dispositivos", color = DarkBlue)
                }

                TextButton(onClick = onRefresh) {
                    Text("Atualizar", color = DarkBlue)
                }
            }
        }

        item {
            Text(
                text = device.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = DarkBlue,
            )

            Text("Animal: ${device.animalName ?: "Não associado"}")
        }

        item {
            ClassroomFeedback(state.error, state.message)
        }

        if (!state.canEdit) {
            item {
                Text(
                    text = when {
                        state.classroomArchived ->
                            "A turma está arquivada. Somente consulta."

                        device.animalId == null ->
                            "Associe um animal ao dispositivo pelo painel administrativo."

                        else ->
                            "O dispositivo precisa estar ativo para alterar os cenários."
                    }
                )
            }
        }

        if (config != null && config.points.isEmpty()) {
            item {
                Text("Nenhum ponto de ausculta disponível para este dispositivo.")
            }
        }

        items(
            items = config?.points.orEmpty(),
            key = { it.pointId },
        ) { point ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = point.position,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkBlue,
                    )

                    Text(
                        text = "Cenário ativo: " +
                                (point.scenario?.name ?: "Não configurado")
                    )

                    point.scenario?.description
                        ?.takeIf { it.isNotBlank() }
                        ?.let { Text(it) }

                    if (state.canEdit) {
                        Button(
                            onClick = { onOpenPoint(point.pointId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("Alterar cenário")
                        }
                    }
                }
            }
        }
    }
}