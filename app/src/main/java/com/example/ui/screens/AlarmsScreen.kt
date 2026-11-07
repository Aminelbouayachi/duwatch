package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*
import com.example.viewmodel.DuWatchViewModel
import java.util.UUID

@Composable
fun AlarmsScreen(viewModel: DuWatchViewModel) {
    val alarms by viewModel.alarms.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<WatchAlarm?>(null) }

    if (showAddDialog || editingAlarm != null) {
        AlarmEditDialog(
            initialAlarm = editingAlarm,
            onDismiss = {
                showAddDialog = false
                editingAlarm = null
            },
            onSave = { alarm ->
                viewModel.saveAlarm(alarm)
                showAddDialog = false
                editingAlarm = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("alarms_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner & Add Alarm Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(NeonLime.copy(alpha = 0.5f), SurfaceBorderDark))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Watch Alarms",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${alarms.count { it.isEnabled }} active • Synced directly to watch buzzer",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_alarm_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Alarms List
        item {
            Text(
                text = "Watch Alarms (${alarms.size})",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (alarms.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AlarmOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Text("No alarms configured", color = TextSecondary, fontSize = 14.sp)
                        Text("Add an alarm to sync it with your smartwatch", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        items(alarms, key = { it.id }) { alarm ->
            AlarmItemCard(
                alarm = alarm,
                onToggle = { viewModel.toggleAlarm(alarm.id) },
                onEdit = { editingAlarm = alarm },
                onDelete = { viewModel.deleteAlarm(alarm.id) }
            )
        }

        // 3. Smart Reminders & Health Alerts Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart Health Reminders",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SmartReminderCard(
                icon = Icons.Default.DirectionsWalk,
                title = "Sedentary Reminder",
                subtitle = "Vibrate watch when inactive for more than 60 mins",
                isEnabled = settings.sedentaryReminder,
                tint = NeonOrange,
                onToggle = {
                    viewModel.updateSettings(settings.copy(sedentaryReminder = !settings.sedentaryReminder))
                }
            )
        }

        item {
            SmartReminderCard(
                icon = Icons.Default.LocalDrink,
                title = "Water Hydration Alert",
                subtitle = "Remind to drink water every 2 hours during day",
                isEnabled = settings.waterReminder,
                tint = NeonCyan,
                onToggle = {
                    viewModel.updateSettings(settings.copy(waterReminder = !settings.waterReminder))
                }
            )
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: WatchAlarm,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = String.format("%02d:%02d", alarm.hour, alarm.minute)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_item_${alarm.id}")
            .clickable { onEdit() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) SurfaceDark else SurfaceDark.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (alarm.isEnabled) SurfaceBorderDark else SurfaceBorderDark.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        color = if (alarm.isEnabled) TextPrimary else TextMuted,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (alarm.hour < 12) "AM" else "PM",
                        color = if (alarm.isEnabled) NeonCyan else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = alarm.label,
                    color = if (alarm.isEnabled) TextSecondary else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Repeat Days Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayLabels.forEachIndexed { index, label ->
                        val dayNum = index + 1
                        val isDayActive = alarm.repeatDays.contains(dayNum)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDayActive && alarm.isEnabled) NeonLime.copy(alpha = 0.2f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isDayActive && alarm.isEnabled) NeonLime else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Smart Wakeup & Vibration Pattern Tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (alarm.smartWakeup) {
                        Box(
                            modifier = Modifier
                                .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Smart Wake (10m)", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(SurfaceVariantDark, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(alarm.vibrationPattern.title, color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDark,
                        checkedTrackColor = NeonLime,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceVariantDark
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_alarm_${alarm.id}")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = HeartRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SmartReminderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    tint: Color,
    onToggle: () -> Unit
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
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BackgroundDark,
                    checkedTrackColor = tint
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    initialAlarm: WatchAlarm?,
    onDismiss: () -> Unit,
    onSave: (WatchAlarm) -> Unit
) {
    var hour by remember { mutableStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableStateOf(initialAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Wake up") }
    var repeatDays by remember { mutableStateOf(initialAlarm?.repeatDays ?: setOf(1, 2, 3, 4, 5)) }
    var smartWakeup by remember { mutableStateOf(initialAlarm?.smartWakeup ?: true) }
    var vibrationPattern by remember { mutableStateOf(initialAlarm?.vibrationPattern ?: VibrationPattern.PULSE) }

    val daysMap = listOf(
        Pair(1, "Mon"),
        Pair(2, "Tue"),
        Pair(3, "Wed"),
        Pair(4, "Thu"),
        Pair(5, "Fri"),
        Pair(6, "Sat"),
        Pair(7, "Sun")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialAlarm == null) "New Watch Alarm" else "Edit Watch Alarm",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time Quick Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { hour = if (hour > 0) hour - 1 else 23 }) {
                            Icon(Icons.Default.Remove, contentDescription = "-1 hour", tint = NeonCyan)
                        }
                        Text(
                            text = String.format("%02d", hour),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan
                        )
                        IconButton(onClick = { hour = if (hour < 23) hour + 1 else 0 }) {
                            Icon(Icons.Default.Add, contentDescription = "+1 hour", tint = NeonCyan)
                        }
                    }

                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    // Minute picker buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { minute = if (minute >= 5) minute - 5 else 55 }) {
                            Icon(Icons.Default.Remove, contentDescription = "-5 min", tint = NeonLime)
                        }
                        Text(
                            text = String.format("%02d", minute),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonLime
                        )
                        IconButton(onClick = { minute = if (minute < 55) minute + 5 else 0 }) {
                            Icon(Icons.Default.Add, contentDescription = "+5 min", tint = NeonLime)
                        }
                    }
                }

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("alarm_label_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan
                    )
                )

                // Repeat Days Chips
                Text("Repeat Days", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysMap.forEach { (dayNum, name) ->
                        val isSelected = repeatDays.contains(dayNum)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonLime else SurfaceVariantDark)
                                .clickable {
                                    val current = repeatDays.toMutableSet()
                                    if (isSelected) current.remove(dayNum) else current.add(dayNum)
                                    repeatDays = current
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1),
                                color = if (isSelected) BackgroundDark else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Smart Wakeup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Gentle Wake-up", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Gentle vibration 10 mins before optimal phase", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = smartWakeup,
                        onCheckedChange = { smartWakeup = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = BackgroundDark, checkedTrackColor = NeonCyan)
                    )
                }

                // Vibration Pattern
                Text("Vibration Pattern", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VibrationPattern.values().forEach { pat ->
                        val isSelected = vibrationPattern == pat
                        FilterChip(
                            selected = isSelected,
                            onClick = { vibrationPattern = pat },
                            label = { Text(pat.title, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonLime,
                                selectedLabelColor = BackgroundDark
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        WatchAlarm(
                            id = initialAlarm?.id ?: UUID.randomUUID().toString(),
                            hour = hour,
                            minute = minute,
                            label = label.ifBlank { "Alarm" },
                            isEnabled = true,
                            repeatDays = repeatDays,
                            smartWakeup = smartWakeup,
                            vibrationPattern = vibrationPattern
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                modifier = Modifier.testTag("save_alarm_button")
            ) {
                Text("Save & Sync", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
