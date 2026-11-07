package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.DuWatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchFaceStudioModal(
    viewModel: DuWatchViewModel,
    onDismiss: () -> Unit
) {
    val draft by viewModel.customFaceDraft.collectAsState()
    var dialName by remember { mutableStateOf(draft.name) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Watch Face Customizer Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_studio_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                viewModel.updateDraftFace(draft.copy(name = dialName))
                                viewModel.applyCustomFace()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 8.dp).testTag("studio_save_sync_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save & Sync", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
                )
            },
            containerColor = BackgroundDark
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Live Interactive Watch Canvas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.3f), SurfaceBorderDark)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIVE WATCH DISPLAY PREVIEW",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        WatchFaceChassis(
                            watchFace = draft,
                            size = 200.dp,
                            showCase = true
                        )
                    }
                }

                // Dial Name Input
                OutlinedTextField(
                    value = dialName,
                    onValueChange = {
                        dialName = it
                        viewModel.updateDraftFace(draft.copy(name = it))
                    },
                    label = { Text("Dial Name", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dial_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceBorderDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // 1. Dial Style: Digital vs Analog
                SectionContainer(title = "1. Dial Display Style") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StyleSelectCard(
                            title = "Digital Modern",
                            subtitle = "Bold LED & LCD Fonts",
                            isSelected = draft.styleType == WatchStyleType.DIGITAL,
                            icon = Icons.Default.Pin,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateDraftFace(draft.copy(styleType = WatchStyleType.DIGITAL)) }
                        )
                        StyleSelectCard(
                            title = "Analog Chrono",
                            subtitle = "Hands & Subdials",
                            isSelected = draft.styleType == WatchStyleType.ANALOG,
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateDraftFace(draft.copy(styleType = WatchStyleType.ANALOG)) }
                        )
                    }
                }

                // 2. Background Wallpaper
                SectionContainer(title = "2. Wallpaper & Background") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Preset Graphic Wallpapers", color = TextSecondary, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WallpaperPresetCard(
                                title = "Cyberpunk",
                                isSelected = draft.backgroundType == BackgroundType.PRESET_IMAGE && draft.bgDrawableRes == R.drawable.bg_watch_cyberpunk,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.updateDraftFace(
                                        draft.copy(
                                            backgroundType = BackgroundType.PRESET_IMAGE,
                                            bgDrawableRes = R.drawable.bg_watch_cyberpunk
                                        )
                                    )
                                }
                            )
                            WallpaperPresetCard(
                                title = "Space Nebula",
                                isSelected = draft.backgroundType == BackgroundType.PRESET_IMAGE && draft.bgDrawableRes == R.drawable.bg_watch_nebula,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.updateDraftFace(
                                        draft.copy(
                                            backgroundType = BackgroundType.PRESET_IMAGE,
                                            bgDrawableRes = R.drawable.bg_watch_nebula
                                        )
                                    )
                                }
                            )
                            WallpaperPresetCard(
                                title = "Deep OLED",
                                isSelected = draft.backgroundType == BackgroundType.SOLID_COLOR && draft.bgSolidHex == "#000000",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.updateDraftFace(
                                        draft.copy(
                                            backgroundType = BackgroundType.SOLID_COLOR,
                                            bgSolidHex = "#000000"
                                        )
                                    )
                                }
                            )
                        }

                        Text("Color Gradients & Tones", color = TextSecondary, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val gradientPresets = listOf(
                                Pair("#0D1B2A", "#1B263B"), // Deep Navy
                                Pair("#1A0022", "#4A0E4E"), // Cyber Violet
                                Pair("#001E17", "#004D40"), // Emerald Matrix
                                Pair("#240000", "#5C0000"), // Crimson Sport
                                Pair("#1C150A", "#423214")  // Gold Amber
                            )

                            gradientPresets.forEach { (start, end) ->
                                val isSelected = draft.backgroundType == BackgroundType.GRADIENT &&
                                        draft.bgSolidHex == start && draft.bgSecondaryHex == end
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(
                                                    try { Color(android.graphics.Color.parseColor(start)) } catch (e: Exception) { NeonCyan },
                                                    try { Color(android.graphics.Color.parseColor(end)) } catch (e: Exception) { NeonPurple }
                                                )
                                            )
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) NeonCyan else SurfaceBorderDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.updateDraftFace(
                                                draft.copy(
                                                    backgroundType = BackgroundType.GRADIENT,
                                                    bgSolidHex = start,
                                                    bgSecondaryHex = end
                                                )
                                            )
                                        }
                                )
                            }
                        }
                    }
                }

                // 3. Time Color & Complication Color
                SectionContainer(title = "3. Dial Color Accent") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Time Digits / Hands Color", color = TextSecondary, fontSize = 12.sp)
                        val colorChoices = listOf(
                            "#00E5FF", // Neon Cyan
                            "#00E676", // Neon Lime
                            "#FF9100", // Neon Orange
                            "#E0AAFF", // Neon Purple
                            "#FFD700", // Gold
                            "#FFFFFF"  // Pure White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colorChoices.forEach { hex ->
                                val isSelected = draft.timeColorHex.equals(hex, ignoreCase = true)
                                val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { NeonCyan }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else SurfaceBorderDark,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.updateDraftFace(draft.copy(timeColorHex = hex))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (draft.styleType == WatchStyleType.DIGITAL) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Time Position", color = TextSecondary, fontSize = 12.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TimePosition.values().forEach { pos ->
                                    val isSelected = draft.timePosition == pos
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateDraftFace(draft.copy(timePosition = pos)) },
                                        label = { Text(pos.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan,
                                            selectedLabelColor = BackgroundDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Complications Widgets
                SectionContainer(title = "4. Watch Face Complications (Data Widgets)") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select widgets to display on the dial (max 3):", color = TextSecondary, fontSize = 12.sp)

                        ComplicationType.values().toList().chunked(2).forEach { rowComps ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowComps.forEach { comp ->
                                    val isEnabled = draft.complications.contains(comp)
                                    ComplicationToggleChip(
                                        complication = comp,
                                        isEnabled = isEnabled,
                                        modifier = Modifier.weight(1f),
                                        onToggle = {
                                            val current = draft.complications.toMutableList()
                                            if (isEnabled) {
                                                current.remove(comp)
                                            } else {
                                                if (current.size < 3) current.add(comp)
                                            }
                                            viewModel.updateDraftFace(draft.copy(complications = current))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun StyleSelectCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else SurfaceVariantDark)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NeonCyan else SurfaceBorderDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                color = if (isSelected) NeonCyan else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun WallpaperPresetCard(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else SurfaceVariantDark)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NeonCyan else SurfaceBorderDark,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) NeonCyan else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ComplicationToggleChip(
    complication: ComplicationType,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val icon = when (complication) {
        ComplicationType.HEART_RATE -> Icons.Default.Favorite
        ComplicationType.STEPS -> Icons.Default.DirectionsWalk
        ComplicationType.BATTERY -> Icons.Default.BatteryChargingFull
        ComplicationType.WEATHER -> Icons.Default.WbSunny
        ComplicationType.CALORIES -> Icons.Default.LocalFireDepartment
        ComplicationType.DATE -> Icons.Default.CalendarToday
        ComplicationType.DISTANCE -> Icons.Default.Place
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isEnabled) NeonLime.copy(alpha = 0.15f) else SurfaceVariantDark)
            .border(
                width = if (isEnabled) 1.5.dp else 1.dp,
                color = if (isEnabled) NeonLime else SurfaceBorderDark,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isEnabled) NeonLime else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = complication.label,
            color = if (isEnabled) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal
        )
    }
}
