package com.nohana.echoes_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nohana.echoes_app.view.states.AuthState
import com.nohana.echoes_app.view.states.LoginState
import com.nohana.echoes_app.data.SessionTokenStore
import com.nohana.echoes_app.network.CurrentUserResult
import com.nohana.echoes_app.network.LoginApi
import com.nohana.echoes_app.network.MfaResult
import com.nohana.echoes_app.service.validation.FieldValidatorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel(
    private val tokenStorage: SessionTokenStore,
    private val loginApi: LoginApi
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Login)
    val loginState = _loginState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    private val _form = MutableStateFlow(AuthForm())
    val form = _form.asStateFlow()

    fun onFormChange(
        email: String? = null, password: String? = null, code: String? = null
    ) {
        _form.update { current ->
            current.copy(
                email = email ?: current.email,
                password = password ?: current.password,
                code = code ?: current.code
            )
        }
    }

    fun login(email: String, password: String) {
        _form.update { it.copy(email = email, password = password) }

        val emailError = FieldValidatorService.validateEmail(email)
        val passwordError = FieldValidatorService.validatePassword(password)

        if (emailError != null || passwordError != null) {
            _loginState.update {
                LoginState.ValidationError(
                    emailError = emailError?.message, passwordError = passwordError?.message
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _loginState.update { LoginState.Loading }

                if (loginApi.login(email, password)) {
                    _loginState.update { LoginState.TwoFactor(email) }
                    _authState.update { AuthState.Unauthenticated }
                } else {
                    _loginState.update { LoginState.Error("Credenciais inválidas") }
                    _authState.update { AuthState.Unauthenticated }
                }
            } catch (e: IOException) {
                _loginState.update { LoginState.Error("Erro de conexão") }
                _authState.update { AuthState.Unauthenticated }
            }
        }
    }

    fun sendTwoFactor(email: String, code: String) {
        _form.update { it.copy(email = email, code = code) }

        val codeError = FieldValidatorService.validateCode(code)
        if (codeError != null) {
            _loginState.value = LoginState.TwoFactor(email, codeError.message)
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                when (val result = loginApi.confirmMfa(email, code)) {
                    is MfaResult.Confirmed -> {
                        when (val profile = loginApi.getCurrentUser(result.token)) {
                            is CurrentUserResult.Found -> {
                                tokenStorage.setToken(result.token)
                                _authState.value = AuthState.Authenticated
                                _loginState.value = LoginState.Success(result.token, profile.user)
                                _form.value = AuthForm()
                            }

                            CurrentUserResult.Unauthorized -> {
                                _authState.value = AuthState.Unauthenticated
                                _loginState.value = LoginState.Login
                            }

                            CurrentUserResult.Failed -> {
                                _loginState.value =
                                    LoginState.Error("Não foi possível carregar o perfil.")
                            }
                        }
                    }

                    MfaResult.InvalidCode -> {
                        _loginState.value = LoginState.TwoFactor(email, "Código inválido")
                    }

                    MfaResult.Failed -> {
                        _loginState.value = LoginState.Error("Não foi possível confirmar o código.")
                    }
                }
            } catch (e: IOException) {
                _loginState.value = LoginState.Error("Erro de conexão.")
            }
        }
    }

    fun validateToken() {
        viewModelScope.launch {
            val token = tokenStorage.getToken()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
                _loginState.value = LoginState.Login
                return@launch
            }

            _loginState.value = LoginState.Loading

            try {
                when (val result = loginApi.getCurrentUser(token)) {
                    is CurrentUserResult.Found -> {
                        _authState.value = AuthState.Authenticated
                        _loginState.value = LoginState.ValidToken(result.user)
                    }

                    CurrentUserResult.Unauthorized -> {
                        tokenStorage.clear()
                        _authState.value = AuthState.Unauthenticated
                        _loginState.value = LoginState.Login
                    }

                    CurrentUserResult.Failed -> {
                        _loginState.value = LoginState.Error("Não foi possível carregar o perfil.")
                    }
                }
            } catch (e: IOException) {
                _loginState.value = LoginState.Error("Erro de conexão.")
            }
        }
    }

    fun logout(
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            val token = tokenStorage.getToken()

            try {
                if (!token.isNullOrBlank() && !loginApi.logout(token)) {
                    onError("Não foi possível encerrar a sessão. Tente novamente.")
                    return@launch
                }

                tokenStorage.clear()
                _form.value = AuthForm()
                _authState.value = AuthState.Unauthenticated
                _loginState.value = LoginState.Login
                onComplete()
            } catch (e: IOException) {
                onError("Erro de conexão ao sair. Tente novamente.")
            }
        }
    }
}

data class AuthForm(
    val email: String? = null, val password: String? = null, val code: String? = null
)