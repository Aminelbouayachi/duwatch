package com.example.data

import com.example.R
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class WatchRepository {

    private val presetWatchFaces = listOf(
        WatchFace(
            id = "wf_cyberpunk",
            name = "Cyberpunk Neon",
            category = "Sci-Fi",
            styleType = WatchStyleType.DIGITAL,
            backgroundType = BackgroundType.PRESET_IMAGE,
            bgDrawableRes = R.drawable.bg_watch_cyberpunk,
            timePosition = TimePosition.CENTER,
            timeColorHex = "#00E5FF",
            complicationColorHex = "#00E676",
            complications = listOf(ComplicationType.HEART_RATE, ComplicationType.STEPS, ComplicationType.BATTERY),
            isInstalled = true,
            author = "DuWatch Cyber"
        ),
        WatchFace(
            id = "wf_nebula",
            name = "Cosmic Nebula",
            category = "Space",
            styleType = WatchStyleType.DIGITAL,
            backgroundType = BackgroundType.PRESET_IMAGE,
            bgDrawableRes = R.drawable.bg_watch_nebula,
            timePosition = TimePosition.TOP,
            timeColorHex = "#E0AAFF",
            complicationColorHex = "#00E5FF",
            complications = listOf(ComplicationType.WEATHER, ComplicationType.DATE, ComplicationType.BATTERY),
            isInstalled = false,
            author = "Astro Studio"
        ),
        WatchFace(
            id = "wf_chrono_sport",
            name = "Chrono Sport X",
            category = "Sports",
            styleType = WatchStyleType.ANALOG,
            backgroundType = BackgroundType.SOLID_COLOR,
            bgSolidHex = "#0D1117",
            bgSecondaryHex = "#161B22",
            timePosition = TimePosition.CENTER,
            timeColorHex = "#00E676",
            complicationColorHex = "#FF9100",
            complications = listOf(ComplicationType.STEPS, ComplicationType.CALORIES, ComplicationType.HEART_RATE),
            isInstalled = false,
            author = "Speed Precision"
        ),
        WatchFace(
            id = "wf_minimal_matrix",
            name = "OLED Matrix",
            category = "Minimal",
            styleType = WatchStyleType.DIGITAL,
            backgroundType = BackgroundType.SOLID_COLOR,
            bgSolidHex = "#000000",
            bgSecondaryHex = "#003311",
            timePosition = TimePosition.CENTER,
            timeColorHex = "#00FF66",
            complicationColorHex = "#00AA44",
            complications = listOf(ComplicationType.STEPS, ComplicationType.BATTERY),
            isInstalled = false,
            author = "Minimalist"
        ),
        WatchFace(
            id = "wf_luxury_gold",
            name = "Tourbillon Heritage",
            category = "Luxury",
            styleType = WatchStyleType.ANALOG,
            backgroundType = BackgroundType.GRADIENT,
            bgSolidHex = "#1A150D",
            bgSecondaryHex = "#2E2413",
            timePosition = TimePosition.CENTER,
            timeColorHex = "#FFD700",
            complicationColorHex = "#E6C200",
            complications = listOf(ComplicationType.DATE, ComplicationType.BATTERY),
            isInstalled = false,
            author = "Geneva Master"
        )
    )

    private val initialAlarms = listOf(
        WatchAlarm(
            id = "alarm_1",
            hour = 6,
            minute = 45,
            label = "Morning Workout",
            isEnabled = true,
            repeatDays = setOf(1, 2, 3, 4, 5),
            smartWakeup = true,
            vibrationPattern = VibrationPattern.PULSE
        ),
        WatchAlarm(
            id = "alarm_2",
            hour = 8,
            minute = 15,
            label = "Take Vitamins & Hydrate",
            isEnabled = true,
            repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
            smartWakeup = false,
            vibrationPattern = VibrationPattern.GENTLE
        ),
        WatchAlarm(
            id = "alarm_3",
            hour = 22,
            minute = 30,
            label = "Sleep Preparation",
            isEnabled = false,
            repeatDays = setOf(1, 2, 3, 4, 5),
            smartWakeup = false,
            vibrationPattern = VibrationPattern.NORMAL
        )
    )

    private val _device = MutableStateFlow(WatchDevice())
    val device: StateFlow<WatchDevice> = _device.asStateFlow()

    private val _watchFaces = MutableStateFlow(presetWatchFaces)
    val watchFaces: StateFlow<List<WatchFace>> = _watchFaces.asStateFlow()

    private val _activeWatchFace = MutableStateFlow(presetWatchFaces.first())
    val activeWatchFace: StateFlow<WatchFace> = _activeWatchFace.asStateFlow()

    private val _alarms = MutableStateFlow(initialAlarms)
    val alarms: StateFlow<List<WatchAlarm>> = _alarms.asStateFlow()

    private val _settings = MutableStateFlow(WatchSettings())
    val settings: StateFlow<WatchSettings> = _settings.asStateFlow()

    private val _healthData = MutableStateFlow(HealthData())
    val healthData: StateFlow<HealthData> = _healthData.asStateFlow()

    fun setActiveWatchFace(watchFace: WatchFace) {
        _activeWatchFace.value = watchFace
        _watchFaces.value = _watchFaces.value.map { face ->
            face.copy(isInstalled = face.id == watchFace.id)
        }
    }

    fun saveCustomWatchFace(customFace: WatchFace) {
        val updatedList = _watchFaces.value.filter { it.id != customFace.id }.toMutableList()
        updatedList.add(0, customFace.copy(isCustom = true, isInstalled = true))
        _watchFaces.value = updatedList
        _activeWatchFace.value = customFace
    }

    fun addAlarm(alarm: WatchAlarm) {
        _alarms.value = _alarms.value + alarm
    }

    fun updateAlarm(updated: WatchAlarm) {
        _alarms.value = _alarms.value.map { if (it.id == updated.id) updated else it }
    }

    fun toggleAlarm(id: String) {
        _alarms.value = _alarms.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun deleteAlarm(id: String) {
        _alarms.value = _alarms.value.filter { it.id != id }
    }

    fun updateSettings(newSettings: WatchSettings) {
        _settings.value = newSettings
    }

    fun toggleAppNotification(appName: String) {
        val current = _settings.value.notifications.toMutableMap()
        current[appName] = !(current[appName] ?: false)
        _settings.value = _settings.value.copy(notifications = current)
    }

    fun updateDeviceConnection(connected: Boolean) {
        _device.value = _device.value.copy(
            isConnected = connected,
            lastSyncTime = if (connected) "Just now" else _device.value.lastSyncTime
        )
    }

    fun updateFirmwareSuccess() {
        val next = _device.value.availableFirmwareUpdate ?: "v2.5.2"
        _device.value = _device.value.copy(
            firmwareVersion = next,
            availableFirmwareUpdate = null
        )
    }

    fun refreshHealthTelemetry(newSteps: Int, newHeartRate: Int, newCalories: Int) {
        _healthData.value = _healthData.value.copy(
            steps = newSteps,
            currentHeartRate = newHeartRate,
            calories = newCalories
        )
    }
}
