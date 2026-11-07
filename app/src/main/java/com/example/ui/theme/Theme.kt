package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = NeonCyan,
    secondary = NeonLime,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = NeonLime,
    tertiary = NeonOrange,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderDark
)

private val LightColorScheme = DarkColorScheme // DuWatch uses an intentional sleek AMOLED dark theme by default

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve high-contrast watch companion styling
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window
                window?.let {
                    WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                    WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
                }
            } catch (e: Throwable) {
                // Safe ignore if insets controller is unavailable in current context
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
