package com.nohana.echoes_app.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClassroomDevice(
    val id: String,
    val name: String? = null,
    val hardwareId: String,
    val status: String,
    val animalId: String? = null,
    val animalName: String? = null,
    val classroomId: String? = null,
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: hardwareId
}

@Serializable
data class DeviceScenarioConfig(
    val deviceId: String,
    val points: List<DevicePointConfig>,
)

@Serializable
data class DevicePointConfig(
    val pointId: String,
    val position: String,
    val sensorIndex: Int? = null,
    val scenario: ActiveScenario? = null,
)

@Serializable
data class ActiveScenario(
    val id: String,
    val name: String,
    val description: String? = null,
    val audioUri: String,
    val audioRevision: String? = null,
)

@Serializable
data class PointScenario(
    val id: String,
    val name: String,
    val description: String? = null,
    val audioPath: String? = null,
    val pointId: String? = null,
)

@Serializable
data class SelectDeviceScenarioRequest(
    val scenarioId: String,
)