package com.nohana.echoes_app.classroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.ClassroomCard
import com.nohana.echoes_app.view.components.ClassroomFeedback

@Composable
fun ClassroomListScreen(
    state: ClassroomState,
    teacher: Boolean,
    onSearch: (String) -> Unit,
    onJoin: () -> Unit,
    onOpen: (String) -> Unit,
    onPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var search by rememberSaveable(state.search) {
        mutableStateOf(state.search)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ClassroomFeedback(state.error, state.message)
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome da turma") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    focusedLabelColor = DarkBlue,
                    cursorColor = DarkBlue,
                ),
            )
        }

        item {
            OutlinedButton(
                onClick = { onSearch(search) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkBlue,
                ),
            ) {
                Text("Buscar turmas")
            }
        }

        if (!teacher) {
            item {
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Entrar com convite")
                }
            }
        }

        if (state.classrooms.isEmpty() && state.error == null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Nenhuma turma encontrada",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkBlue,
                    )

                    Text(
                        text = if (teacher) {
                            "Suas turmas aparecerão aqui quando você for vinculado."
                        } else {
                            "Utilize o convite do professor para entrar em uma turma."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(
            items = state.classrooms,
            key = { it.id },
        ) { classroom ->
            ClassroomCard(
                classroom = classroom,
                onClick = { onOpen(classroom.id) },
            )
        }

        if (state.pageCount > 1) {
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

                    Text("${state.page + 1} de ${state.pageCount}")

                    TextButton(
                        onClick = { onPage(state.page + 1) },
                        enabled = state.page + 1 < state.pageCount,
                    ) {
                        Text("Próxima")
                    }
                }
            }
        }
    }
}