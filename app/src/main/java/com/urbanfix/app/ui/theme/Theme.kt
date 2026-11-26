package com.urbanfix.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val GreenPrimary = Color(0xFF16A34A)
val GreenLightAccent = Color(0xFF4ADE80)
val GreenDark = Color(0xFF15803D)
val GreenLightBg = Color(0xFFF0FDF4)
val GreenDarkBg = Color(0xFF14532D)
val AlarmRed = Color(0xFFDC2626)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenLightBg,
    onPrimaryContainer = GreenDark,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    background = Color(0xFFF9FAFB),
    surface = Color.White,
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111),
    onSurfaceVariant = Color(0xFF64748B),
    outlineVariant = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFFF1F5F9),
    error = AlarmRed
)

private val DarkColors = darkColorScheme(
    primary = GreenLightAccent,
    onPrimary = Color(0xFF052E16),
    primaryContainer = GreenDarkBg,
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF334155),
    surfaceVariant = Color(0xFF334155),
    error = Color(0xFFF87171)
)

@Composable
fun UrbanFixTheme(dark: Boolean, largeText: Boolean, content: @Composable () -> Unit) {
    val base = LocalDensity.current
    val density = remember(base, largeText) {
        Density(base.density, base.fontScale * if (largeText) 1.3f else 1f)
    }
    CompositionLocalProvider(LocalDensity provides density) {
        MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
    }
}