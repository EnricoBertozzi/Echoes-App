package com.nohana.echoes_app.view.activities.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.nohana.echoes_app.R
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.ui.theme.EchoesAppTheme
import com.nohana.echoes_app.view.components.ConfirmationDialog
import com.nohana.echoes_app.view.components.TitleComponent

/**
 * Tela de configurações do aplicativo.
 *
 * Ainda sem conteúdo. As opções serão definidas e implementadas
 * em momento posterior.
 */
@Composable
fun SettingsScreen(
    onPrivacy: () -> Unit,
    onSecurity: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: (code: String) -> Unit
) {

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    ConfirmationDialog(
        showDialog = showLogoutDialog,
        title = "Sair da Conta",
        message = "Deseja realmente sair da sua conta?",
        confirmText = "Sair",
        confirmColor = DarkBlue,
        onConfirm = {
            showLogoutDialog = false
            onLogout()
        },
        onDismiss = {
            showLogoutDialog = false
        }
    )



        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            SettingsItem(
                title = "Privacidade",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null
                    )
                },
                onClick = onPrivacy
            )

            SettingsItem(
                title = "Segurança",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.shield),
                        contentDescription = null
                    )
                },
                onClick = onSecurity
            )

            SettingsItem(
                title = "Logout",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.logout),
                        contentDescription = null,
                        tint = Color.Black
                    )
                },
                onClick = {
                    showLogoutDialog = true
                }
            )

    }
}

@Preview()
@Composable
fun SettingsScreenPreview() {
    EchoesAppTheme(
        darkTheme = false
    ) {
        SettingsScreen(
            onLogout = { },
            onPrivacy = { },
            onSecurity = { },
            onDeleteAccount = { }
        )
    }
}

