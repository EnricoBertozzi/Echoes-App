package com.nohana.echoes_app.activities.states

import com.nohana.echoes_app.network.UserProfile

sealed interface LoginState {
    object Loading : LoginState
    data class Error(val error: String = ""): LoginState

    data class TwoFactor(val email: String, val error: String = ""): LoginState

    object Login: LoginState

    data class ValidToken(val user: UserProfile) : LoginState

    data class Success(
        val token: String,
        val user: UserProfile,
    ) : LoginState
    data class ValidationError(val emailError: String? = "", val passwordError: String? = ""): LoginState

}