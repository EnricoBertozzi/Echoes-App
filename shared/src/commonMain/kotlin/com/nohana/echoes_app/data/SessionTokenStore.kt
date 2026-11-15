package com.nohana.echoes_app.data

interface SessionTokenStore {
    fun getToken(): String?
    fun setToken(token: String)
    fun clear()
}