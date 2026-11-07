package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.WatchDevice
import com.example.ui.theme.*
import com.example.viewmodel.DuWatchViewModel

@Composable
fun SmartFeaturesScreen(viewModel: DuWatchViewModel) {
    val device by viewModel.device.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isFindingWatch by viewModel.isFindingWatch.collectAsState()
    val cameraFlash by viewModel.cameraFlash.collectAsState()
    val cameraPhotoCount by viewModel.cameraTriggerCount.collectAsState()
    val isScanningBle by viewModel.isScanningBle.collectAsState()
    val nearbyDevices by viewModel.nearbyDevices.collectAsState()

    var showBleScanModal by remember { mutableStateOf(false) }
    var showCameraModal by remember { mutableStateOf(false) }

    if (showBleScanModal) {
        BleDeviceScanDialog(
            isScanning = isScanningBle,
            devices = nearbyDevices,
            currentDevice = device,
            onStartScan = { viewModel.startBleScan() },
            onConnect = { dev ->
                viewModel.connectToDevice(dev)
                showBleScanModal = false
            },
            onDismiss = { showBleScanModal = false }
        )
    }

    if (showCameraModal) {
        CameraShutterDialog(
            photoCount = cameraPhotoCount,
            flash = cameraFlash,
            onTriggerShutter = { viewModel.triggerRemoteShutter() },
            onDismiss = { showCameraModal = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("smart_features_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Interactive Tool Cards Row (Find My Watch + Remote Camera)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Find My Watch Hero Button
                FindWatchCard(
                    isFinding = isFindingWatch,
                    modifier = Modifier.weight(1f),
                    onToggleFind = {
                        if (isFindingWatch) viewModel.stopFindMyWatch()
                        else viewModel.triggerFindMyWatch()
                    }
                )

                // Remote Camera Shutter Card
                RemoteCameraCard(
                    photoCount = cameraPhotoCount,
                    modifier = Modifier.weight(1f),
                    onOpen = { showCameraModal = true }
                )
            }
        }

        // 2. Watch System Toggles (Raise to Wake, 24/7 HR, Weather, DND)
        item {
            Text(
                text = "Watch Hardware Controls",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FeatureToggleCard(
                icon = Icons.Default.PanTool,
                title = "Raise to Wake Wrist Gesture",
                subtitle = "Turn on watch screen automatically when raising your arm",
                isChecked = settings.raiseToWake,
                tint = NeonCyan,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(raiseToWake = it))
                }
            )
        }

        item {
            FeatureToggleCard(
                icon = Icons.Default.Favorite,
                title = "Continuous 24/7 Heart Rate",
                subtitle = "Track cardiac rhythms and abnormal pulse alerts every 10 min",
                isChecked = settings.continuousHeartRate,
                tint = HeartRed,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(continuousHeartRate = it))
                }
            )
        }

        item {
            FeatureToggleCard(
                icon = Icons.Default.WbSunny,
                title = "Weather Location Push",
                subtitle = "Push live forecast (${settings.weatherCity} • ${settings.weatherTempCelsius}°C) to watch dial",
                isChecked = settings.weatherSync,
                tint = NeonOrange,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(weatherSync = it))
                }
            )
        }

        item {
            FeatureToggleCard(
                icon = Icons.Default.DoNotDisturbOn,
                title = "Do Not Disturb (DND) Mode",
                subtitle = "Mute vibrations and screen wake from ${settings.dndStartHour}:00 to ${settings.dndEndHour}:00",
                isChecked = settings.dndEnabled,
                tint = NeonPurple,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(dndEnabled = it))
                }
            )
        }

        // 3. Vibration Strength & Watch Formats
        item {
            HardwarePreferencesCard(
                settings = settings,
                onUpdate = { viewModel.updateSettings(it) }
            )
        }

        // 4. Notifications Forwarding Center
        item {
            Text(
                text = "App Alerts & Notifications",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            NotificationsManagerCard(
                notifications = settings.notifications,
                onToggleApp = { viewModel.toggleAppNotification(it) }
            )
        }

        // 5. Device Manager & OTA Firmware
        item {
            Text(
                text = "Device & Firmware Manager",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DeviceManagementCard(
                device = device,
                onScanDevices = {
                    viewModel.startBleScan()
                    showBleScanModal = true
                },
                onOtaUpdate = { viewModel.startOtaUpdate() }
            )
        }
    }
}

@Composable
fun FindWatchCard(
    isFinding: Boolean,
    modifier: Modifier = Modifier,
    onToggleFind: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .testTag("feature_find_watch_card")
            .clickable { onToggleFind() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isFinding) NeonCyan else SurfaceBorderDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = if (isFinding) 0.3f else 0.15f))
                    .border(1.5.dp, NeonCyan, CircleShape)
                    .then(if (isFinding) Modifier.scale(pulseScale) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Radar",
                    tint = NeonCyan,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = if (isFinding) "Buzzer Ringing…" else "Find My Watch",
                color = if (isFinding) NeonCyan else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isFinding) "Tap to Stop" else "Send sound signal",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun RemoteCameraCard(
    photoCount: Int,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag("feature_camera_card")
            .clickable { onOpen() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NeonOrange.copy(alpha = 0.15f))
                    .border(1.5.dp, NeonOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = NeonOrange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Remote Shutter",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (photoCount > 0) "$photoCount photos taken" else "Control camera",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun FeatureToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    tint: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                }

                Column {
                    Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = TextSecondary, fontSize = 11.sp)
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BackgroundDark,
                    checkedTrackColor = tint
                )
            )
        }
    }
}

@Composable
fun HardwarePreferencesCard(
    settings: com.example.model.WatchSettings,
    onUpdate: (com.example.model.WatchSettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Hardware Preferences", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            // Vibration Intensity Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Vibration Motor Strength", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val levels = listOf(Pair(1, "Mild"), Pair(2, "Standard"), Pair(3, "Strong"))
                    levels.forEach { (lvl, title) ->
                        val isSelected = settings.vibrationStrength == lvl
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdate(settings.copy(vibrationStrength = lvl)) },
                            label = { Text(title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonLime,
                                selectedLabelColor = BackgroundDark
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Time Format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("24-Hour Military Time Format", color = TextPrimary, fontSize = 13.sp)
                Switch(
                    checked = settings.timeFormat24h,
                    onCheckedChange = { onUpdate(settings.copy(timeFormat24h = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = BackgroundDark, checkedTrackColor = NeonCyan)
                )
            }
        }
    }
}

@Composable
fun NotificationsManagerCard(
    notifications: Map<String, Boolean>,
    onToggleApp: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Push alerts from your phone directly to watch OLED display:",
                color = TextSecondary,
                fontSize = 12.sp
            )

            notifications.forEach { (app, isEnabled) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val icon = when (app) {
                            "WhatsApp" -> Icons.Default.Chat
                            "Phone Calls" -> Icons.Default.Call
                            "SMS Messages" -> Icons.Default.Sms
                            "Instagram" -> Icons.Default.Camera
                            "Messenger" -> Icons.Default.Forum
                            "Gmail" -> Icons.Default.Mail
                            else -> Icons.Default.Notifications
                        }
                        Icon(icon, contentDescription = null, tint = if (isEnabled) NeonCyan else TextMuted, modifier = Modifier.size(18.dp))
                        Text(app, color = if (isEnabled) TextPrimary else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggleApp(app) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundDark,
                            checkedTrackColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("notification_toggle_$app")
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceManagementCard(
    device: WatchDevice,
    onScanDevices: () -> Unit,
    onOtaUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(device.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("MAC: ${device.macAddress} • RSSI: ${device.signalStrengthDbm} dBm", color = TextMuted, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onScanDevices,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("scan_ble_button")
                ) {
                    Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Switch / Pair", fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = SurfaceBorderDark)

            // Firmware update banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Firmware Version", color = TextSecondary, fontSize = 12.sp)
                    Text(device.firmwareVersion, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                if (device.availableFirmwareUpdate != null) {
                    Button(
                        onClick = onOtaUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("ota_update_button")
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update ${device.availableFirmwareUpdate}", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    Text("Up to date", color = NeonLime, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun BleDeviceScanDialog(
    isScanning: Boolean,
    devices: List<WatchDevice>,
    currentDevice: WatchDevice,
    onStartScan: () -> Unit,
    onConnect: (WatchDevice) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nearby Smartwatches", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                    }
                }

                if (isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(20.dp))
                        Text("Scanning Bluetooth Low Energy…", color = NeonCyan, fontSize = 13.sp)
                    }
                }

                devices.forEach { dev ->
                    val isConnected = dev.id == currentDevice.id && currentDevice.isConnected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .clickable { onConnect(dev) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dev.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${dev.macAddress} • ${dev.signalStrengthDbm} dBm", color = TextMuted, fontSize = 11.sp)
                        }

                        if (isConnected) {
                            Text("Connected", color = NeonLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            FilledTonalButton(
                                onClick = { onConnect(dev) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Connect", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = onStartScan,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rescan BLE Devices", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CameraShutterDialog(
    photoCount: Int,
    flash: Boolean,
    onTriggerShutter: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (flash) Color.White else BackgroundDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Watch Remote Shutter", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                    }
                }

                // Camera Viewfinder Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF151922))
                        .border(1.dp, NeonOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = NeonOrange,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Live Viewfinder Active", color = TextSecondary, fontSize = 13.sp)
                        Text("Shake watch or tap shutter button", color = TextMuted, fontSize = 11.sp)
                    }

                    // Framing Target
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    )
                }

                Text(
                    text = "$photoCount Photos Captured via Watch",
                    color = NeonLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = onTriggerShutter,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snap_photo_button")
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Watch Shutter", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
