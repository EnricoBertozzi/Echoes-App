package com.nohana.echoes_app.view.activities.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nohana.echoes_app.network.NetworkProvider
import com.nohana.echoes_app.ui.theme.EchoesAppTheme
import com.nohana.echoes_app.view.activities.home.StudentActivity
import com.nohana.echoes_app.view.activities.home.TeacherActivity
import com.nohana.echoes_app.view.activities.password.PasswordResetActivity
import com.nohana.echoes_app.view.components.TitleComponent
import com.nohana.echoes_app.view.components.LoadingScreen
import com.nohana.echoes_app.view.states.LoginState
import com.nohana.echoes_app.viewmodel.AuthViewModel
import com.nohana.echoes_app.viewmodel.factory.AuthViewModelFactory

class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            NetworkProvider.getAddress(),
            applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoesAppTheme {
                Column {
                    TitleComponent("Login")

                    AuthRoute(
                        viewModel = viewModel,
                        onForgotPassword = {
                            startActivity(
                                Intent(this@AuthActivity, PasswordResetActivity::class.java)
                            )
                        },
                        onAuthenticated = { user ->
                            val destination = if (user.role == "TEACHER") {
                                TeacherActivity::class.java
                            } else {
                                StudentActivity::class.java
                            }

                            startActivity(Intent(this@AuthActivity, destination))
                            finish()
                        },
                    )
                }
            }
        }
    }
}