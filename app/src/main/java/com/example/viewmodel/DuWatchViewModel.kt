package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WatchRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DuWatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WatchRepository()

    val device: StateFlow<WatchDevice> = repository.device
    val watchFaces: StateFlow<List<WatchFace>> = repository.watchFaces
    val activeWatchFace: StateFlow<WatchFace> = repository.activeWatchFace
    val alarms: StateFlow<List<WatchAlarm>> = repository.alarms
    val settings: StateFlow<WatchSettings> = repository.settings
    val healthData: StateFlow<HealthData> = repository.healthData

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // In-progress Custom Face Editor state
    private val _customFaceDraft = MutableStateFlow(
        WatchFace(
            id = "custom_" + UUID.randomUUID().toString().take(6),
            name = "My Custom Dial",
            category = "Custom",
            styleType = WatchStyleType.DIGITAL,
            backgroundType = BackgroundType.GRADIENT,
            bgSolidHex = "#0E1626",
            bgSecondaryHex = "#00E5FF",
            timePosition = TimePosition.CENTER,
            timeColorHex = "#00E5FF",
            complicationColorHex = "#00E676",
            complications = listOf(ComplicationType.HEART_RATE, ComplicationType.STEPS, ComplicationType.BATTERY),
            isCustom = true
        )
    )
    val customFaceDraft: StateFlow<WatchFace> = _customFaceDraft.asStateFlow()

    // Find My Watch state
    private val _isFindingWatch = MutableStateFlow(false)
    val isFindingWatch: StateFlow<Boolean> = _isFindingWatch.asStateFlow()

    // Remote camera shutter state
    private val _cameraTriggerCount = MutableStateFlow(0)
    val cameraTriggerCount: StateFlow<Int> = _cameraTriggerCount.asStateFlow()

    private val _cameraFlash = MutableStateFlow(false)
    val cameraFlash: StateFlow<Boolean> = _cameraFlash.asStateFlow()

    // Nearby BLE Devices scanning modal state
    private val _isScanningBle = MutableStateFlow(false)
    val isScanningBle: StateFlow<Boolean> = _isScanningBle.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<List<WatchDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<WatchDevice>> = _nearbyDevices.asStateFlow()

    private fun getVibrator(): Vibrator? {
        val context = getApplication<Application>()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrateDevice(patternMs: LongArray) {
        val vibrator = getVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(patternMs, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(patternMs, -1)
        }
    }

    // --- Watch Faces ---
    fun selectWatchFace(face: WatchFace) {
        repository.setActiveWatchFace(face)
        syncWatchFaceToWatch(face)
    }

    fun updateDraftFace(updated: WatchFace) {
        _customFaceDraft.value = updated
    }

    fun applyCustomFace() {
        val draft = _customFaceDraft.value
        repository.saveCustomWatchFace(draft)
        syncWatchFaceToWatch(draft)
        // prepare next fresh draft
        _customFaceDraft.value = draft.copy(
            id = "custom_" + UUID.randomUUID().toString().take(6),
            name = "Custom Dial " + (10..99).random()
        )
    }

    fun syncWatchFaceToWatch(watchFace: WatchFace) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing(
                title = "Syncing Dial to Watch",
                progress = 0.05f,
                detail = "Establishing high-speed BLE connection…"
            )
            delay(500)

            _syncState.value = SyncState.Syncing(
                title = "Pushing Dial: ${watchFace.name}",
                progress = 0.30f,
                detail = "Packing dial resources & compression…"
            )
            delay(600)

            _syncState.value = SyncState.Syncing(
                title = "Transferring to DuWatch Storage",
                progress = 0.75f,
                detail = "Writing binary firmware layout: 1.4 MB / 1.8 MB…"
            )
            delay(700)

            _syncState.value = SyncState.Syncing(
                title = "Installing Dial",
                progress = 0.95f,
                detail = "Verifying CRC32 checksum…"
            )
            delay(400)

            vibrateDevice(longArrayOf(0, 80, 50, 120))
            _syncState.value = SyncState.Success("Watch face synced successfully!")
            delay(1200)
            _syncState.value = SyncState.Idle
        }
    }

    // --- Alarms ---
    fun toggleAlarm(id: String) {
        repository.toggleAlarm(id)
        quickSyncAlarmsNotification()
    }

    fun saveAlarm(alarm: WatchAlarm) {
        val existing = alarms.value.find { it.id == alarm.id }
        if (existing != null) {
            repository.updateAlarm(alarm)
        } else {
            repository.addAlarm(alarm)
        }
        quickSyncAlarmsNotification()
    }

    fun deleteAlarm(id: String) {
        repository.deleteAlarm(id)
        quickSyncAlarmsNotification()
    }

    private fun quickSyncAlarmsNotification() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing(
                title = "Updating Watch Alarms",
                progress = 0.5f,
                detail = "Writing alarm schedule to watch EEPROM…"
            )
            delay(400)
            _syncState.value = SyncState.Success("Alarms synced with watch!")
            delay(800)
            _syncState.value = SyncState.Idle
        }
    }

    // --- Smart Features ---
    fun triggerFindMyWatch() {
        viewModelScope.launch {
            _isFindingWatch.value = true
            // Vibrate phone rhythmically
            vibrateDevice(longArrayOf(0, 200, 100, 200, 100, 400))
            delay(4000)
            _isFindingWatch.value = false
        }
    }

    fun stopFindMyWatch() {
        _isFindingWatch.value = false
    }

    fun triggerRemoteShutter() {
        viewModelScope.launch {
            _cameraFlash.value = true
            vibrateDevice(longArrayOf(0, 60))
            _cameraTriggerCount.value += 1
            delay(150)
            _cameraFlash.value = false
        }
    }

    fun toggleAppNotification(app: String) {
        repository.toggleAppNotification(app)
    }

    fun updateSettings(newSettings: WatchSettings) {
        repository.updateSettings(newSettings)
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing(
                title = "Syncing Telemetry",
                progress = 0.4f,
                detail = "Reading sensor registers: HR, SpO2, Pedometer…"
            )
            delay(600)
            val current = healthData.value
            repository.refreshHealthTelemetry(
                newSteps = current.steps + (15..85).random(),
                newHeartRate = (68..84).random(),
                newCalories = current.calories + (2..8).random()
            )
            _syncState.value = SyncState.Success("Health data up to date!")
            delay(800)
            _syncState.value = SyncState.Idle
        }
    }

    fun startBleScan() {
        viewModelScope.launch {
            _isScanningBle.value = true
            _nearbyDevices.value = emptyList()
            delay(800)
            _nearbyDevices.value = listOf(
                WatchDevice(
                    id = "DW-ULTRA-01",
                    name = "DuWatch Ultra 2",
                    modelNumber = "DW-X900-Pro",
                    macAddress = "E4:65:B8:2A:91:0F",
                    batteryPercent = 86,
                    signalStrengthDbm = -54,
                    isConnected = true
                ),
                WatchDevice(
                    id = "DW-PRO-02",
                    name = "DuWatch Pro Sport",
                    modelNumber = "DW-P800",
                    macAddress = "F1:34:CC:89:12:4A",
                    batteryPercent = 92,
                    signalStrengthDbm = -68,
                    isConnected = false
                ),
                WatchDevice(
                    id = "DW-BAND-03",
                    name = "DuWatch Smart Band 9",
                    modelNumber = "DW-B100",
                    macAddress = "A2:99:11:44:00:88",
                    batteryPercent = 64,
                    signalStrengthDbm = -78,
                    isConnected = false
                )
            )
            _isScanningBle.value = false
        }
    }

    fun connectToDevice(device: WatchDevice) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing(
                title = "Pairing with ${device.name}",
                progress = 0.5f,
                detail = "Exchanging GATT security keys…"
            )
            delay(700)
            repository.updateDeviceConnection(true)
            _syncState.value = SyncState.Success("Paired and connected to ${device.name}!")
            delay(900)
            _syncState.value = SyncState.Idle
        }
    }

    fun startOtaUpdate() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing(
                title = "Firmware OTA Update",
                progress = 0.15f,
                detail = "Downloading firmware package v2.5.2 (14.2 MB)…"
            )
            delay(800)
            _syncState.value = SyncState.Syncing(
                title = "Flashing Watch ROM",
                progress = 0.65f,
                detail = "Writing block 420/800…"
            )
            delay(900)
            _syncState.value = SyncState.Syncing(
                title = "Rebooting Watch",
                progress = 0.95f,
                detail = "Restarting DuWatch OS v2.5.2…"
            )
            delay(500)
            repository.updateFirmwareSuccess()
            vibrateDevice(longArrayOf(0, 100, 100, 150))
            _syncState.value = SyncState.Success("Firmware updated to v2.5.2!")
            delay(1000)
            _syncState.value = SyncState.Idle
        }
    }

    fun dismissSyncState() {
        _syncState.value = SyncState.Idle
    }
}
