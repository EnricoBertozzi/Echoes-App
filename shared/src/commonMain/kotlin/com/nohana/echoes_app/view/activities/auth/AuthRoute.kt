package com.nohana.echoes_app.view.activities.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nohana.echoes_app.view.states.LoginState
import com.nohana.echoes_app.network.UserProfile
import com.nohana.echoes_app.view.components.LoadingScreen
import com.nohana.echoes_app.viewmodel.AuthViewModel

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onForgotPassword: () -> Unit,
    onAuthenticated: (UserProfile) -> Unit,
) {
    val state by viewModel.loginState.collectAsState()
    val form by viewModel.form.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.validateToken()
    }

    when (val current = state) {
        LoginState.Loading -> LoadingScreen()

        is LoginState.TwoFactor -> TwoFactorScreen(
            email = current.email,
            code = form.code.orEmpty(),
            onCodeChange = { viewModel.onFormChange(code = it) },
            onTwoFactor = viewModel::sendTwoFactor,
            error = current.error,
        )

        is LoginState.Success -> LaunchedEffect(current) {
            onAuthenticated(current.user)
        }

        is LoginState.ValidToken -> LaunchedEffect(current) {
            onAuthenticated(current.user)
        }

        LoginState.Login,
        is LoginState.Error,
        is LoginState.ValidationError -> {
            val validation = current as? LoginState.ValidationError

            LoginScreen(
                email = form.email.orEmpty(),
                password = form.password.orEmpty(),
                onEmailChange = { viewModel.onFormChange(email = it) },
                onPasswordChange = { viewModel.onFormChange(password = it) },
                onLogin = viewModel::login,
                error = (current as? LoginState.Error)?.error.orEmpty(),
                emailError = validation?.emailError,
                passwordError = validation?.passwordError,
                onForgotPassword = onForgotPassword,
            )
        }
    }
}