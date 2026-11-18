package com.nohana.echoes_app.classroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.network.dto.PointScenario
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.ClassroomFeedback
import com.nohana.echoes_app.viewmodel.DeviceScenarioState

@Composable
fun PointScenarioScreen(
    state: DeviceScenarioState,
    onBack: () -> Unit,
    onPage: (Int) -> Unit,
    onSelect: (PointScenario) -> Unit,
) {
    val point = state.point ?: return

    var pending by remember(point.pointId) {
        mutableStateOf<PointScenario?>(null)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("Voltar aos pontos", color = DarkBlue)
            }
        }

        item {
            Text(
                text = point.position,
                style = MaterialTheme.typography.titleLarge,
                color = DarkBlue,
            )

            Text(
                text = "Atual: ${point.scenario?.name ?: "Não configurado"}"
            )
        }

        item {
            ClassroomFeedback(state.error, state.message)
        }

        item {
            TextButton(onClick = { onPage(state.page) }) {
                Text("Atualizar cenários", color = DarkBlue)
            }
        }

        if (state.scenarios.isEmpty() && state.error == null) {
            item {
                Text(
                    if (state.page == 0) {
                        "Nenhum cenário cadastrado para este ponto."
                    } else {
                        "Não há mais cenários nesta página."
                    }
                )
            }
        }

        items(
            items = state.scenarios,
            key = { it.id },
        ) { scenario ->
            val active = scenario.id == point.scenario?.id
            val compatible = scenario.pointId == point.pointId

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = scenario.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkBlue,
                    )

                    scenario.description
                        ?.takeIf { it.isNotBlank() }
                        ?.let { Text(it) }

                    OutlinedButton(
                        onClick = { pending = scenario },
                        enabled = !active && compatible && state.canEdit,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (active) "Cenário ativo" else "Usar este cenário"
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onPage(state.page - 1) },
                    enabled = state.page > 0,
                ) {
                    Text("Anterior")
                }

                Text("Página ${state.page + 1}")

                TextButton(
                    onClick = { onPage(state.page + 1) },
                    enabled = state.hasNextPage,
                ) {
                    Text("Próxima")
                }
            }
        }
    }

    pending?.let { scenario ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Alterar cenário ativo?") },
            text = {
                Text(
                    "Usar “${scenario.name}” no ponto “${point.position}” " +
                            "do dispositivo “${state.device?.displayName}”?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pending = null
                        onSelect(scenario)
                    },
                ) {
                    Text("Confirmar", color = DarkBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}