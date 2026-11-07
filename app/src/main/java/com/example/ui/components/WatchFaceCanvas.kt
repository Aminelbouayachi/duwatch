package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WatchFaceChassis(
    modifier: Modifier = Modifier,
    watchFace: WatchFace,
    size: Dp = 260.dp,
    showCase: Boolean = true,
    timeText: String? = null,
    dateText: String? = null
) {
    Box(
        modifier = modifier
            .size(width = size + 36.dp, height = size + 44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showCase) {
            // Watch Strap Top Accent
            Box(
                modifier = Modifier
                    .width(size * 0.46f)
                    .height(28.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF1E2430), Color(0xFF121620))
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )

            // Watch Strap Bottom Accent
            Box(
                modifier = Modifier
                    .width(size * 0.46f)
                    .height(28.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF121620), Color(0xFF1E2430))
                        ),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            )

            // Outer Titanium Bezel & Crown
            Box(
                modifier = Modifier
                    .size(size + 24.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2E384D), Color(0xFF151922), Color(0xFF0D1017))
                        )
                    )
                    .border(2.dp, Color(0xFF3E4A63), CircleShape)
            )

            // Digital Crown Button (Right edge)
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(26.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 6.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF8A99AD), Color(0xFF384354), Color(0xFF1B222E))
                        ),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }

        // The Smartwatch Circular Screen
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            WatchFaceContent(
                watchFace = watchFace,
                size = size,
                timeText = timeText,
                dateText = dateText
            )
        }
    }
}

@Composable
fun WatchFaceContent(
    watchFace: WatchFace,
    size: Dp,
    timeText: String? = null,
    dateText: String? = null
) {
    // Current simulated live time
    val currentTime = remember {
        val cal = Calendar.getInstance()
        try {
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEE dd", Locale.getDefault())
            Pair(
                timeText ?: sdfTime.format(cal.time),
                dateText ?: sdfDate.format(cal.time).uppercase(Locale.getDefault())
            )
        } catch (e: Throwable) {
            Pair(timeText ?: "10:42", dateText ?: "MON 07")
        }
    }

    val parsedTimeColor = remember(watchFace.timeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(watchFace.timeColorHex))
        } catch (e: Exception) {
            NeonCyan
        }
    }

    val parsedComplicationColor = remember(watchFace.complicationColorHex) {
        try {
            Color(android.graphics.Color.parseColor(watchFace.complicationColorHex))
        } catch (e: Exception) {
            NeonLime
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Layer
        when (watchFace.backgroundType) {
            BackgroundType.PRESET_IMAGE -> {
                if (watchFace.bgDrawableRes != null) {
                    Image(
                        painter = painterResource(id = watchFace.bgDrawableRes),
                        contentDescription = "Watch background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Semi-transparent overlay to ensure readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
            }
            BackgroundType.GRADIENT -> {
                val start = remember(watchFace.bgSolidHex) {
                    try { Color(android.graphics.Color.parseColor(watchFace.bgSolidHex)) } catch (e: Exception) { Color(0xFF101726) }
                }
                val end = remember(watchFace.bgSecondaryHex) {
                    try { Color(android.graphics.Color.parseColor(watchFace.bgSecondaryHex)) } catch (e: Exception) { Color(0xFF1E2E4A) }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(end, start, Color.Black)
                            )
                        )
                )
            }
            BackgroundType.SOLID_COLOR -> {
                val solidColor = remember(watchFace.bgSolidHex) {
                    try { Color(android.graphics.Color.parseColor(watchFace.bgSolidHex)) } catch (e: Exception) { Color.Black }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(solidColor)
                )
            }
            BackgroundType.CUSTOM_URI -> {
                // Fallback elegant dark carbon texture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF1B2333), Color(0xFF0F141F), Color.Black)
                            )
                        )
                )
            }
        }

        // Circular Bezel Markers
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.toPx() / 2f
            val center = Offset(radius, radius)

            // Outer gauge ring
            drawCircle(
                color = parsedTimeColor.copy(alpha = 0.15f),
                radius = radius - 8f,
                style = Stroke(width = 2f)
            )

            // Hour tick marks around border
            for (i in 0 until 60) {
                val isHour = i % 5 == 0
                val tickLength = if (isHour) 12f else 6f
                val strokeWidth = if (isHour) 2.5f else 1.2f
                val tickColor = if (isHour) parsedTimeColor.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.2f)

                val angle = (i * 6).toDouble()
                val angleRad = Math.toRadians(angle - 90)
                val startX = center.x + (radius - 14f) * cos(angleRad).toFloat()
                val startY = center.y + (radius - 14f) * sin(angleRad).toFloat()
                val endX = center.x + (radius - 14f - tickLength) * cos(angleRad).toFloat()
                val endY = center.y + (radius - 14f - tickLength) * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Main Dial Content: Digital vs Analog
        if (watchFace.styleType == WatchStyleType.DIGITAL) {
            DigitalDialLayout(
                watchFace = watchFace,
                timeString = currentTime.first,
                dateString = currentTime.second,
                timeColor = parsedTimeColor,
                complicationColor = parsedComplicationColor,
                canvasSize = size
            )
        } else {
            AnalogDialLayout(
                watchFace = watchFace,
                dateString = currentTime.second,
                handColor = parsedTimeColor,
                accentColor = parsedComplicationColor,
                canvasSize = size
            )
        }
    }
}

@Composable
fun DigitalDialLayout(
    watchFace: WatchFace,
    timeString: String,
    dateString: String,
    timeColor: Color,
    complicationColor: Color,
    canvasSize: Dp
) {
    val scale = (canvasSize.value / 260f).coerceIn(0.4f, 1.2f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((18 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = when (watchFace.timePosition) {
            TimePosition.TOP -> Arrangement.SpaceBetween
            TimePosition.CENTER -> Arrangement.Center
            TimePosition.BOTTOM -> Arrangement.SpaceBetween
        }
    ) {
        if (watchFace.timePosition == TimePosition.BOTTOM) {
            // Complications on Top
            ComplicationsRow(
                complications = watchFace.complications.take(2),
                color = complicationColor,
                scale = scale
            )
        }

        // Main Digital Clock
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = (4 * scale).dp)
        ) {
            // Optional Date Pill above/below
            Text(
                text = dateString,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = (13 * scale).sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )

            // Bold Glowing Time Digits
            Text(
                text = timeString,
                color = timeColor,
                fontSize = (54 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
            )

            // Pulse or Battery mini bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((4 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((6 * scale).dp)
                        .background(NeonLime, CircleShape)
                )
                Text(
                    text = "SYNCED",
                    color = NeonLime,
                    fontSize = (9 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (watchFace.timePosition != TimePosition.BOTTOM) {
            // Complications below
            ComplicationsRow(
                complications = watchFace.complications,
                color = complicationColor,
                scale = scale
            )
        }
    }
}

@Composable
fun AnalogDialLayout(
    watchFace: WatchFace,
    dateString: String,
    handColor: Color,
    accentColor: Color,
    canvasSize: Dp
) {
    val scale = (canvasSize.value / 260f).coerceIn(0.4f, 1.2f)

    // Animated sweeping second hand
    val infiniteTransition = rememberInfiniteTransition(label = "secondHand")
    val secondRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondSweep"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Date Complication at 3 o'clock
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-26 * scale).dp)
                .background(Color(0xFF1A2230), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF36445D), RoundedCornerShape(4.dp))
                .padding(horizontal = (6 * scale).dp, vertical = (2 * scale).dp)
        ) {
            Text(
                text = dateString,
                color = Color.White,
                fontSize = (10 * scale).sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Complications at Top & Bottom Subdials
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((28 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top subdial
            if (watchFace.complications.isNotEmpty()) {
                SingleComplicationBadge(
                    complication = watchFace.complications.first(),
                    color = accentColor,
                    scale = scale
                )
            }

            // Bottom subdial
            if (watchFace.complications.size > 1) {
                SingleComplicationBadge(
                    complication = watchFace.complications[1],
                    color = accentColor,
                    scale = scale
                )
            }
        }

        // Analog Hands Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.width / 2f
            val center = Offset(radius, radius)

            // Hour Hand (pointing roughly at 10)
            rotate(degrees = 310f, pivot = center) {
                drawLine(
                    color = Color.White,
                    start = Offset(center.x, center.y + 12f * scale),
                    end = Offset(center.x, center.y - (radius * 0.48f)),
                    strokeWidth = 6.5f * scale,
                    cap = StrokeCap.Round
                )
                // Inset hand stripe
                drawLine(
                    color = handColor,
                    start = Offset(center.x, center.y - 10f * scale),
                    end = Offset(center.x, center.y - (radius * 0.44f)),
                    strokeWidth = 2.5f * scale,
                    cap = StrokeCap.Round
                )
            }

            // Minute Hand (pointing at 42 mins = 252 degrees)
            rotate(degrees = 252f, pivot = center) {
                drawLine(
                    color = Color.White,
                    start = Offset(center.x, center.y + 14f * scale),
                    end = Offset(center.x, center.y - (radius * 0.72f)),
                    strokeWidth = 4.5f * scale,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = handColor,
                    start = Offset(center.x, center.y - 12f * scale),
                    end = Offset(center.x, center.y - (radius * 0.68f)),
                    strokeWidth = 1.8f * scale,
                    cap = StrokeCap.Round
                )
            }

            // Smooth Sweeping Seconds Hand (Neon Orange/Lime)
            rotate(degrees = secondRotation, pivot = center) {
                drawLine(
                    color = accentColor,
                    start = Offset(center.x, center.y + 20f * scale),
                    end = Offset(center.x, center.y - (radius * 0.84f)),
                    strokeWidth = 2f * scale,
                    cap = StrokeCap.Round
                )
                // Seconds tail circle
                drawCircle(
                    color = accentColor,
                    radius = 4f * scale,
                    center = Offset(center.x, center.y + 14f * scale)
                )
            }

            // Central Cap
            drawCircle(
                color = Color.Black,
                radius = 7f * scale,
                center = center
            )
            drawCircle(
                color = handColor,
                radius = 4.5f * scale,
                center = center
            )
        }
    }
}

@Composable
fun ComplicationsRow(
    complications: List<ComplicationType>,
    color: Color,
    scale: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        complications.forEach { comp ->
            SingleComplicationBadge(
                complication = comp,
                color = color,
                scale = scale
            )
        }
    }
}

@Composable
fun SingleComplicationBadge(
    complication: ComplicationType,
    color: Color,
    scale: Float
) {
    val (icon, text) = when (complication) {
        ComplicationType.HEART_RATE -> Pair(Icons.Default.Favorite, "72 BPM")
        ComplicationType.STEPS -> Pair(Icons.Default.DirectionsWalk, "8.4k")
        ComplicationType.BATTERY -> Pair(Icons.Default.BatteryChargingFull, "86%")
        ComplicationType.WEATHER -> Pair(Icons.Default.WbSunny, "24°C")
        ComplicationType.CALORIES -> Pair(Icons.Default.LocalFireDepartment, "485")
        ComplicationType.DATE -> Pair(Icons.Default.CalendarToday, "NOV 07")
        ComplicationType.DISTANCE -> Pair(Icons.Default.Place, "6.2 km")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((3 * scale).dp),
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape((10 * scale).dp))
            .border(0.7.dp, color.copy(alpha = 0.4f), RoundedCornerShape((10 * scale).dp))
            .padding(horizontal = (6 * scale).dp, vertical = (3 * scale).dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = complication.label,
            tint = if (complication == ComplicationType.HEART_RATE) HeartRed else color,
            modifier = Modifier.size((11 * scale).dp)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = (10 * scale).sp,
            fontWeight = FontWeight.Medium
        )
    }
}
