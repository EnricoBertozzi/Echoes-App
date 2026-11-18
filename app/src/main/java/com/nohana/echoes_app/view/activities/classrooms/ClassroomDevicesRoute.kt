package com.nohana.echoes_app.classroom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.nohana.echoes_app.ui.theme.DarkBlue
import com.nohana.echoes_app.view.components.LoadingScreen
import com.nohana.echoes_app.viewmodel.DeviceScenarioViewModel

@Composable
fun ClassroomDevicesRoute(
    classroomId: String,
    classroomArchived: Boolean,
    viewModel: DeviceScenarioViewModel,
    onBackToClassroom: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(
        viewModel,
        classroomId,
        classroomArchived,
    ) {
        viewModel.loadClassroom(
            classroomId = classroomId,
            archived = classroomArchived,
        )

        onDispose {
            viewModel.clear()
        }
    }

    val onBack: () -> Unit = {
        if (!state.busy) {
            if (state.point != null || state.device != null) {
                viewModel.back()
            } else {
                onBackToClassroom()
            }
        }
    }

    BackHandler(onBack = onBack)

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = DarkBlue,
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            when {
                state.busy || state.classroomId != classroomId -> {
                    LoadingScreen()
                }

                state.point != null -> {
                    PointScenarioScreen(
                        state = state,
                        onBack = onBack,
                        onPage = { viewModel.loadScenarios(it) },
                        onSelect = { viewModel.selectScenario(it) },
                    )
                }

                state.device != null -> {
                    DeviceClinicalScenarioScreen(
                        state = state,
                        onBack = onBack,
                        onRefresh = { viewModel.refreshConfig() },
                        onOpenPoint = { viewModel.openPoint(it) },
                    )
                }

                else -> {
                    ClassroomDevicesScreen(
                        state = state,
                        onBack = onBack,
                        onRefresh = { viewModel.refreshDevices() },
                        onOpenDevice = { viewModel.openDevice(it) },
                    )
                }
            }
        }
    }
}