package com.nohana.echoes_app.view.activities.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nohana.echoes_app.MainActivity
import com.nohana.echoes_app.network.NetworkProvider
import com.nohana.echoes_app.view.activities.security.SecurityActivity
import com.nohana.echoes_app.view.activities.settings.PrivacyActivity
import com.nohana.echoes_app.view.activities.settings.SettingsScreen
import com.nohana.echoes_app.view.components.LoadingScreen
import com.nohana.echoes_app.view.screens.ProfileScreen
import com.nohana.echoes_app.view.states.UserDeleteEvent
import com.nohana.echoes_app.viewmodel.AuthViewModel
import com.nohana.echoes_app.viewmodel.UserViewModel
import com.nohana.echoes_app.viewmodel.factory.AuthViewModelFactory
import com.nohana.echoes_app.viewmodel.factory.UserViewModelFactory

class StudentActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(NetworkProvider.getAddress(), applicationContext)
    }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(NetworkProvider.getAddress(), applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by userViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                authViewModel.validateToken()
                userViewModel.getUserInfo()
            }

            LaunchedEffect(Unit) {
                userViewModel.event.collect { event ->
                    when (event) {
                        UserDeleteEvent.DeleteAccountSuccess -> openMainActivity()
                        is UserDeleteEvent.Error -> Unit
                        is UserDeleteEvent.DeleteRequestSucess -> Unit
                    }
                }
            }

            StudentScreen { selectedTab ->
                if (uiState.isLoading && uiState.user == null) {
                    LoadingScreen()
                } else {
                    uiState.user?.let { user ->
                        when (selectedTab) {
                            StudentTab.CLASS -> HomeScreen()

                            StudentTab.PROFILE -> ProfileScreen(user = user)

                            StudentTab.SETTINGS -> SettingsScreen(
                                onLogout = {
                                    authViewModel.logout(
                                        onComplete = ::openMainActivity,
                                        onError = { message ->
                                            Toast.makeText(
                                                this@StudentActivity,
                                                message,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        },
                                    )
                                },
                                onPrivacy = {
                                    startActivity(
                                        Intent(this@StudentActivity, PrivacyActivity::class.java)
                                    )
                                },
                                onSecurity = {
                                    startActivity(
                                        Intent(this@StudentActivity, SecurityActivity::class.java)
                                    )
                                },
                                onDeleteAccount = userViewModel::deleteAccount,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}