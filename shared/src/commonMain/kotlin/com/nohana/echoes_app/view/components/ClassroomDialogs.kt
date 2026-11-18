package com.nohana.echoes_app.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nohana.echoes_app.network.dto.ClassroomMaterial
import com.nohana.echoes_app.ui.theme.DarkBlue

@Composable
fun ClassroomJoinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    val valid = Regex("[A-HJ-NP-Z2-9]{6}").matches(code)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entrar em uma turma", color = DarkBlue) },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.trim().uppercase().take(6)
                },
                label = { Text("Código de convite") },
                supportingText = { Text("Informe os 6 caracteres do convite.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(code) },
                enabled = valid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = Color.White,
                ),
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
fun ClassroomMaterialDialog(
    material: ClassroomMaterial? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var title by rememberSaveable(material?.id) {
        mutableStateOf(material?.title.orEmpty())
    }

    var description by rememberSaveable(material?.id) {
        mutableStateOf(material?.description.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (material == null) {
                    "Adicionar material"
                } else {
                    "Editar material"
                },
                color = DarkBlue,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(150) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(1000) },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(title.trim(), description.trim())
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    if (material == null) "Escolher arquivo" else "Salvar"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
fun ClassroomDeleteMaterialDialog(
    material: ClassroomMaterial,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remover material?") },
        text = {
            Text("Deseja remover “${material.title}” da turma?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Remover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}