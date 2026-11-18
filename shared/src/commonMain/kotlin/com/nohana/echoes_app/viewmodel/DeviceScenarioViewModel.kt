package com.nohana.echoes_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nohana.echoes_app.classroom.ClassroomApi
import com.nohana.echoes_app.classroom.ClassroomApiException
import com.nohana.echoes_app.network.dto.ClassroomDevice
import com.nohana.echoes_app.network.dto.DevicePointConfig
import com.nohana.echoes_app.network.dto.DeviceScenarioConfig
import com.nohana.echoes_app.network.dto.PointScenario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceScenarioState(
    val classroomId: String? = null,
    val classroomArchived: Boolean = false,
    val devices: List<ClassroomDevice> = emptyList(),
    val device: ClassroomDevice? = null,
    val config: DeviceScenarioConfig? = null,
    val point: DevicePointConfig? = null,
    val scenarios: List<PointScenario> = emptyList(),
    val page: Int = 0,
    val hasNextPage: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null,
) {
    val canEdit: Boolean
        get() = !classroomArchived &&
                device?.status == "ACTIVE" &&
                device.animalId != null
}

class DeviceScenarioViewModel(
    private val api: ClassroomApi,
) : ViewModel() {

    private val mutableState = MutableStateFlow(DeviceScenarioState())
    val state = mutableState.asStateFlow()

    private var operation: Job? = null
    private var generation = 0

    fun loadClassroom(
        classroomId: String,
        archived: Boolean,
    ) {
        clear()

        mutableState.value = DeviceScenarioState(
            classroomId = classroomId,
            classroomArchived = archived,
        )

        execute {
            val devices = api.devices(classroomId)

            mutableState.update {
                it.copy(devices = devices)
            }
        }
    }

    fun refreshDevices() {
        val current = state.value
        val id = current.classroomId ?: return

        if (current.busy) return

        execute {
            val devices = api.devices(id)
            mutableState.update { it.copy(devices = devices) }
        }
    }

    fun openDevice(device: ClassroomDevice) {
        val current = state.value

        if (current.busy || current.devices.none { it.id == device.id }) {
            return
        }

        mutableState.update {
            it.copy(
                device = device,
                config = null,
                point = null,
                scenarios = emptyList(),
            )
        }

        refreshConfig()
    }

    fun refreshConfig() {
        val current = state.value
        val device = current.device ?: return

        if (current.busy) return

        execute {
            val config = api.deviceConfig(device.id)

            mutableState.update {
                it.copy(config = config)
            }
        }
    }

    fun openPoint(pointId: String) {
        val current = state.value

        if (current.busy || !current.canEdit) return

        val point = current.config?.points
            ?.firstOrNull { it.pointId == pointId }
            ?: return

        mutableState.update {
            it.copy(
                point = point,
                scenarios = emptyList(),
                page = 0,
                hasNextPage = false,
            )
        }

        loadScenarios(0)
    }

    fun loadScenarios(page: Int) {
        val current = state.value
        val point = current.point ?: return

        if (current.busy || page < 0) return

        execute {
            val scenarios = api.pointScenarios(
                pointId = point.pointId,
                page = page,
                size = PAGE_SIZE,
            )

            mutableState.update {
                it.copy(
                    scenarios = scenarios,
                    page = page,
                    hasNextPage = scenarios.size == PAGE_SIZE,
                )
            }
        }
    }

    fun selectScenario(scenario: PointScenario) {
        val current = state.value
        val device = current.device ?: return
        val point = current.point ?: return

        if (current.busy || !current.canEdit) return
        if (scenario.id == point.scenario?.id) return

        if (current.scenarios.none {
                it.id == scenario.id && it.pointId == point.pointId
            }
        ) {
            return
        }

        execute {
            api.selectDeviceScenario(
                deviceId = device.id,
                pointId = point.pointId,
                scenarioId = scenario.id,
            )

            mutableState.update {
                it.copy(
                    point = null,
                    scenarios = emptyList(),
                    config = null,
                    message = "Configuração salva.",
                )
            }

            try {
                val config = api.deviceConfig(device.id)
                mutableState.update { it.copy(config = config) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.update {
                    it.copy(
                        error = "A alteração foi salva, mas não foi possível " +
                                "recarregar os pontos. Toque em Atualizar.",
                        message = null,
                    )
                }
            }
        }
    }

    fun back() {
        val current = state.value
        if (current.busy) return

        mutableState.update {
            when {
                it.point != null -> it.copy(
                    point = null,
                    scenarios = emptyList(),
                    error = null,
                    message = null,
                )

                it.device != null -> it.copy(
                    device = null,
                    config = null,
                    error = null,
                    message = null,
                )

                else -> it
            }
        }
    }

    fun clear() {
        generation++
        operation?.cancel()
        operation = null
        mutableState.value = DeviceScenarioState()
    }

    private fun execute(action: suspend () -> Unit) {
        if (state.value.busy) return

        val currentGeneration = ++generation

        mutableState.update {
            it.copy(busy = true, error = null, message = null)
        }

        operation = viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClassroomApiException) {
                mutableState.update {
                    it.copy(error = e.message ?: "A operação falhou.")
                }
            } catch (_: Exception) {
                mutableState.update {
                    it.copy(
                        error = "Não foi possível carregar ou salvar os dados. " +
                                "Verifique sua conexão e tente novamente."
                    )
                }
            } finally {
                if (generation == currentGeneration) {
                    mutableState.update { it.copy(busy = false) }
                }
            }
        }
    }

    override fun onCleared() {
        operation?.cancel()
        api.close()
        super.onCleared()
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}