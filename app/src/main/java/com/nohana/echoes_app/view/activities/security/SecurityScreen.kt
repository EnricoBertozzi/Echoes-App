package com.nohana.echoes_app.view.activities.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.nohana.echoes_app.R
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.ui.theme.EchoesAppTheme
import com.nohana.echoes_app.view.activities.settings.SettingsItem
import com.nohana.echoes_app.view.components.InfoDialog
import com.nohana.echoes_app.view.components.TextDialog
import com.nohana.echoes_app.view.components.TitleComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onBackFunction: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDeleteAccount: (code: String) -> Unit,
    onChangePassword: () -> Unit,
    showDeleteDialog: Boolean,
    showDeleteSuccessDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDismissDeleteSuccess: () -> Unit,
    snackbar: SnackbarHostState
) {

    TextDialog(
        showDialog = showDeleteDialog,
        title = "Excluir Conta",
        message =
            "Um código de confirmação foi enviado para seu email.\n\n" +
                    "Digite o código abaixo para confirmar a exclusão da conta.",
        confirmText = "Excluir",
        confirmColor = Color.Red,

        onConfirm = { code ->
            onConfirm()
            onDeleteAccount(code)
        },
        onDismiss = onDismiss
    )

    InfoDialog(
        showDialog = showDeleteSuccessDialog,
        title = "Conta Excluída",
        message =
            "Sua conta foi excluída com sucesso.",
        onDismiss = onDismissDeleteSuccess
    )

    Scaffold(
        topBar = {
            TitleComponent(
                "Segurança",
                onBackFunction
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbar
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            SettingsItem(
                title = "Alterar Senha",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = null,
                        tint = DarkBlue
                    )
                },
                onClick = onChangePassword
            )

            SettingsItem(
                title = "Excluir Conta",
                textColor = Color.Red,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = null,
                        tint = Color.Red
                    )
                },
                onClick = {
                    onDeleteRequest()
                }
            )

        }
    }
}

@Preview
@Composable
fun SecurityScreenPreview() {

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    EchoesAppTheme(
        darkTheme = false
    ) {
        SecurityScreen(
            onBackFunction = { },
            onDeleteAccount = { },
            onDeleteRequest = { },
            onChangePassword = { },
            showDeleteDialog = false,
            showDeleteSuccessDialog = false,
            onConfirm = { },
            onDismiss = { },
            snackbar = snackbarHostState,
            onDismissDeleteSuccess = { }
        )
    }
}