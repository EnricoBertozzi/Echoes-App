package com.nohana.echoes_app.classroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.network.dto.ClassroomDevice
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.ClassroomFeedback
import com.nohana.echoes_app.viewmodel.DeviceScenarioState

@Composable
fun ClassroomDevicesScreen(
    state: DeviceScenarioState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenDevice: (ClassroomDevice) -> Unit,
) {
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
                    Text("Voltar à turma", color = DarkBlue)
                }

                TextButton(onClick = onRefresh) {
                    Text("Atualizar", color = DarkBlue)
                }
            }
        }

        item {
            Text(
                text = "Dispositivos da turma",
                style = MaterialTheme.typography.titleLarge,
                color = DarkBlue,
            )
        }

        item {
            ClassroomFeedback(state.error, state.message)
        }

        if (state.classroomArchived) {
            item {
                Text("Turma arquivada. A configuração fica disponível para consulta.")
            }
        }

        if (state.devices.isEmpty() && state.error == null) {
            item {
                Text(
                    text = "Nenhum dispositivo vinculado a esta turma.",
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        }

        items(
            items = state.devices,
            key = { it.id },
        ) { device ->
            OutlinedCard(
                onClick = { onOpenDevice(device) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = device.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkBlue,
                    )

                    Text(
                        text = "Animal: ${device.animalName ?: "Não associado"}"
                    )

                    Text(
                        text = when (device.status) {
                            "ACTIVE" -> "Ativo"
                            "INACTIVE" -> "Inativo"
                            "PENDING" -> "Pendente de aprovação"
                            else -> device.status
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Text(
                        text = "Consultar pontos e cenários",
                        color = DarkBlue,
                    )
                }
            }
        }
    }
}