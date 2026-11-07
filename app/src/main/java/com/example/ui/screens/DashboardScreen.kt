package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.WatchFaceChassis
import com.example.ui.theme.*
import com.example.viewmodel.DuWatchViewModel

@Composable
fun DashboardScreen(
    viewModel: DuWatchViewModel,
    onNavigateToFaces: () -> Unit,
    onNavigateToAlarms: () -> Unit,
    onNavigateToFeatures: () -> Unit
) {
    val device by viewModel.device.collectAsState()
    val activeFace by viewModel.activeWatchFace.collectAsState()
    val health by viewModel.healthData.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Device Hero Card
        item {
            DeviceHeroCard(
                device = device,
                onSyncClick = { viewModel.refreshTelemetry() },
                onManageClick = onNavigateToFeatures
            )
        }

        // 2. Active Watch Face Preview & Quick Customizer Callout
        item {
            ActiveWatchFaceBanner(
                activeFace = activeFace,
                onCustomizeClick = onNavigateToFaces
            )
        }

        // 3. Quick Action Buttons Row
        item {
            QuickActionsBar(
                onFindWatch = { viewModel.triggerFindMyWatch() },
                onCameraShutter = onNavigateToFeatures,
                onAddAlarm = onNavigateToAlarms,
                onSyncTelemetry = { viewModel.refreshTelemetry() }
            )
        }

        // 4. Daily Activity Progress (Steps, Calories, Distance)
        item {
            ActivityRingsCard(health = health)
        }

        // 5. Hourly Activity Bar Chart
        item {
            HourlyStepsChartCard(hourlySteps = health.hourlySteps)
        }

        // 6. Health & Vitals Section (Heart Rate, SpO2, Sleep)
        item {
            VitalsSection(
                health = health,
                onMeasureHeartRate = { viewModel.refreshTelemetry() }
            )
        }
    }
}

@Composable
fun DeviceHeroCard(
    device: WatchDevice,
    onSyncClick: () -> Unit,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(NeonCyan.copy(alpha = 0.5f), SurfaceBorderDark)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = "Watch Icon",
                            tint = NeonCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = device.name,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (device.isConnected) NeonLime else HeartRed,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (device.isConnected) "Connected (BLE)" else "Disconnected",
                                color = if (device.isConnected) NeonLime else HeartRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "•  ${device.firmwareVersion}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Battery Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${device.batteryPercent}%",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = SurfaceBorderDark)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last synced: ${device.lastSyncTime}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onSyncClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NeonCyan.copy(alpha = 0.15f),
                            contentColor = NeonCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("hero_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    FilledTonalButton(
                        onClick = onManageClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SurfaceVariantDark,
                            contentColor = TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("hero_manage_button")
                    ) {
                        Text("Manage", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveWatchFaceBanner(
    activeFace: WatchFace,
    onCustomizeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_watch_face_banner")
            .clickable { onCustomizeClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(NeonPurple.copy(alpha = 0.5f), SurfaceBorderDark)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE WATCH FACE",
                            color = NeonPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activeFace.name,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Style: ${activeFace.styleType.name.lowercase().replaceFirstChar { it.uppercase() }} • ${activeFace.category}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onCustomizeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("customize_face_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Customize",
                        tint = BackgroundDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Change / Customize Dial",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Compact Watch Chassis Preview
            WatchFaceChassis(
                watchFace = activeFace,
                size = 100.dp,
                showCase = true,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun QuickActionsBar(
    onFindWatch: () -> Unit,
    onCameraShutter: () -> Unit,
    onAddAlarm: () -> Unit,
    onSyncTelemetry: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.Radar,
            label = "Find Watch",
            tint = NeonCyan,
            modifier = Modifier.weight(1f),
            onClick = onFindWatch,
            testTag = "quick_find_watch"
        )
        QuickActionButton(
            icon = Icons.Default.CameraAlt,
            label = "Remote Camera",
            tint = NeonOrange,
            modifier = Modifier.weight(1f),
            onClick = onCameraShutter,
            testTag = "quick_camera"
        )
        QuickActionButton(
            icon = Icons.Default.Alarm,
            label = "Set Alarm",
            tint = NeonLime,
            modifier = Modifier.weight(1f),
            onClick = onAddAlarm,
            testTag = "quick_alarm"
        )
        QuickActionButton(
            icon = Icons.Default.Refresh,
            label = "Sync Data",
            tint = NeonPurple,
            modifier = Modifier.weight(1f),
            onClick = onSyncTelemetry,
            testTag = "quick_refresh"
        )
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(SurfaceBorderDark, SurfaceVariantDark))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActivityRingsCard(health: HealthData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_rings_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(SurfaceBorderDark, Color.Transparent))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity & Goals",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(health.steps.toFloat() / health.stepsGoal * 100).toInt()}% completed",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Steps Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Steps",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Steps", color = TextSecondary, fontSize = 13.sp)
                    }
                    Text(
                        "${health.steps} / ${health.stepsGoal}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (health.steps.toFloat() / health.stepsGoal).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonCyan,
                    trackColor = SurfaceVariantDark
                )
            }

            // Calories & Distance Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Calories",
                                tint = NeonOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Active Burn", color = TextSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${health.calories} kcal",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Distance
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Distance",
                                tint = NeonLime,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Distance", color = TextSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${health.distanceKm} km",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyStepsChartCard(hourlySteps: List<Int>) {
    val maxSteps = remember(hourlySteps) { (hourlySteps.maxOrNull() ?: 1000).coerceAtLeast(500) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hourly_steps_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(SurfaceBorderDark, Color.Transparent))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Hourly Distribution",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Peak: $maxSteps steps/hr",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                hourlySteps.forEachIndexed { index, steps ->
                    val heightRatio = (steps.toFloat() / maxSteps).coerceIn(0.08f, 1f)
                    val isPeak = steps == maxSteps

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(heightRatio)
                                .background(
                                    if (isPeak) NeonCyan else NeonCyan.copy(alpha = 0.35f),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("6 AM", color = TextMuted, fontSize = 10.sp)
                Text("12 PM", color = TextMuted, fontSize = 10.sp)
                Text("6 PM", color = TextMuted, fontSize = 10.sp)
                Text("10 PM", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun VitalsSection(
    health: HealthData,
    onMeasureHeartRate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Vitals & Health Sensors",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Heart Rate Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("vital_heart_rate_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.verticalGradient(listOf(HeartRed.copy(alpha = 0.4f), SurfaceBorderDark))
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart Rate",
                            tint = HeartRed,
                            modifier = Modifier.size(20.dp)
                        )
                        IconButton(
                            onClick = onMeasureHeartRate,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Measure",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${health.currentHeartRate} BPM",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Resting: ${health.restingHeartRate} | Range: ${health.minHeartRate}-${health.maxHeartRate}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // SpO2 Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("vital_spo2_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.4f), SurfaceBorderDark))
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "SpO2",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "${health.spO2}%",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Blood Oxygen • Optimal",
                        color = NeonLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Sleep Analysis Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vital_sleep_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.verticalGradient(listOf(NeonPurple.copy(alpha = 0.4f), SurfaceBorderDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep",
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Sleep Breakdown", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${health.sleepTotalMinutes / 60}h ${health.sleepTotalMinutes % 60}m",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sleep Stage Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    // Deep Sleep
                    Box(
                        modifier = Modifier
                            .weight(health.deepSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF4A148C))
                    )
                    // Light Sleep
                    Box(
                        modifier = Modifier
                            .weight(health.lightSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF7C4DFF))
                    )
                    // REM Sleep
                    Box(
                        modifier = Modifier
                            .weight(health.remSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF00E5FF))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SleepStageLegend(color = Color(0xFF4A148C), label = "Deep: ${health.deepSleepMinutes / 60}h")
                    SleepStageLegend(color = Color(0xFF7C4DFF), label = "Light: ${health.lightSleepMinutes / 60}h")
                    SleepStageLegend(color = Color(0xFF00E5FF), label = "REM: ${health.remSleepMinutes}m")
                }
            }
        }
    }
}

@Composable
fun SleepStageLegend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}
