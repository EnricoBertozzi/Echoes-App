package com.nohana.echoes_app.activities.states

sealed interface AuthState {
    object Authenticated: AuthState
    object Unauthenticated: AuthState
}