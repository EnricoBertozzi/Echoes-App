package com.nohana.echoes_app.view.activities.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nohana.echoes_app.resources.Res
import com.nohana.echoes_app.resources.home
import com.nohana.echoes_app.resources.person
import com.nohana.echoes_app.resources.settings
import com.nohana.echoes_app.ui.theme.DarkBlue
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class StudentTab(
    val label: String,
    val icon: DrawableResource,
) {
    PROFILE("Perfil", Res.drawable.person),
    CLASS("Turmas", Res.drawable.home),
    SETTINGS("Configurações", Res.drawable.settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    content: @Composable (StudentTab) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(StudentTab.CLASS) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(selectedTab.label) },
            )
        },
        bottomBar = {
            NavigationBar(containerColor = DarkBlue) {
                StudentTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(tab.icon),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                            )
                        },
                        label = { Text(tab.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBlue,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color.White,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            content(selectedTab)
        }
    }
}