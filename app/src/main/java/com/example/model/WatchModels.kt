package com.example.model

enum class WatchStyleType {
    DIGITAL,
    ANALOG
}

enum class BackgroundType {
    PRESET_IMAGE,
    GRADIENT,
    SOLID_COLOR,
    CUSTOM_URI
}

enum class ComplicationType(val label: String, val iconName: String) {
    HEART_RATE("Heart Rate", "favorite"),
    STEPS("Steps", "directions_walk"),
    BATTERY("Battery", "battery_charging_full"),
    WEATHER("Weather", "wb_sunny"),
    CALORIES("Calories", "local_fire_department"),
    DATE("Date", "calendar_today"),
    DISTANCE("Distance", "place")
}

enum class TimePosition {
    TOP,
    CENTER,
    BOTTOM
}

enum class VibrationPattern(val title: String) {
    GENTLE("Gentle Wave"),
    NORMAL("Standard Buzz"),
    PULSE("Double Pulse"),
    INTENSE("Ultra Shock")
}

data class WatchFace(
    val id: String,
    val name: String,
    val category: String,
    val styleType: WatchStyleType = WatchStyleType.DIGITAL,
    val backgroundType: BackgroundType = BackgroundType.SOLID_COLOR,
    val bgDrawableRes: Int? = null,
    val bgSolidHex: String = "#0A0E17",
    val bgSecondaryHex: String = "#00E5FF",
    val customImageUri: String? = null,
    val timePosition: TimePosition = TimePosition.CENTER,
    val timeColorHex: String = "#00E5FF",
    val complicationColorHex: String = "#00E676",
    val complications: List<ComplicationType> = listOf(
        ComplicationType.HEART_RATE,
        ComplicationType.STEPS,
        ComplicationType.BATTERY
    ),
    val isCustom: Boolean = false,
    val isInstalled: Boolean = false,
    val author: String = "DuWatch Studio"
)

data class WatchDevice(
    val id: String = "DW-ULTRA-01",
    val name: String = "DuWatch Ultra 2",
    val modelNumber: String = "DW-X900-Pro",
    val macAddress: String = "E4:65:B8:2A:91:0F",
    val batteryPercent: Int = 86,
    val isCharging: Boolean = false,
    val signalStrengthDbm: Int = -58,
    val isConnected: Boolean = true,
    val firmwareVersion: String = "v2.4.0",
    val availableFirmwareUpdate: String? = "v2.5.2",
    val lastSyncTime: String = "Just now"
)

data class WatchAlarm(
    val id: String,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = setOf(1, 2, 3, 4, 5), // 1=Mon, 7=Sun
    val smartWakeup: Boolean = true,
    val vibrationPattern: VibrationPattern = VibrationPattern.PULSE
)

data class WatchSettings(
    val raiseToWake: Boolean = true,
    val dndEnabled: Boolean = false,
    val dndStartHour: Int = 23,
    val dndEndHour: Int = 7,
    val sedentaryReminder: Boolean = true,
    val sedentaryIntervalMinutes: Int = 60,
    val waterReminder: Boolean = true,
    val waterIntervalHours: Int = 2,
    val continuousHeartRate: Boolean = true,
    val vibrationStrength: Int = 2, // 1 to 3
    val unitSystem: String = "Metric (km)",
    val timeFormat24h: Boolean = true,
    val weatherSync: Boolean = true,
    val weatherTempCelsius: Int = 24,
    val weatherCity: String = "Casablanca",
    val notifications: Map<String, Boolean> = mapOf(
        "WhatsApp" to true,
        "Phone Calls" to true,
        "SMS Messages" to true,
        "Instagram" to false,
        "Messenger" to true,
        "Gmail" to true,
        "Telegram" to false
    )
)

data class HealthData(
    val steps: Int = 8420,
    val stepsGoal: Int = 10000,
    val calories: Int = 485,
    val caloriesGoal: Int = 650,
    val distanceKm: Float = 6.2f,
    val currentHeartRate: Int = 72,
    val minHeartRate: Int = 58,
    val maxHeartRate: Int = 138,
    val restingHeartRate: Int = 64,
    val spO2: Int = 98,
    val sleepTotalMinutes: Int = 450, // 7h 30m
    val deepSleepMinutes: Int = 135,
    val lightSleepMinutes: Int = 240,
    val remSleepMinutes: Int = 75,
    val hourlySteps: List<Int> = listOf(0, 0, 0, 0, 0, 120, 650, 1240, 890, 1420, 950, 1100, 720, 680, 650, 0),
    val hrHistory: List<Int> = listOf(62, 65, 68, 71, 75, 84, 92, 110, 88, 74, 72, 70)
)

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val title: String, val progress: Float, val detail: String) : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val error: String) : SyncState()
}
