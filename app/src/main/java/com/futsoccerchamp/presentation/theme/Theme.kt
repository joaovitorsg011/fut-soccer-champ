package com.futsoccerchamp.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val NavyDeep = Color(0xFF050F2B)
val Navy = Color(0xFF0E2559)
val NavyPrimary = Color(0xFF1B3F9B)
val NavyLight = Color(0xFF3C69D6)
val Gold = Color(0xFFE0A82E)
val GoldLight = Color(0xFFFFCB45)
val GoldDark = Color(0xFFA5761A)

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE4FA),
    onPrimaryContainer = Navy,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDEFCB),
    onSecondaryContainer = Color(0xFF4A3406),
    tertiary = Gold,
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF12151F),
    surface = Color.White,
    onSurface = Color(0xFF12151F),
    surfaceVariant = Color(0xFFE3E6F0),
    onSurfaceVariant = Color(0xFF4A4F60),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F8FC),
    surfaceContainer = Color(0xFFF1F3F9),
    surfaceContainerHigh = Color(0xFFEBEEF6),
    surfaceContainerHighest = Color(0xFFE5E9F3),
    outline = Color(0xFF767C8C),
    outlineVariant = Color(0xFFC6CBDA),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = Color.White,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = Color(0xFFDCE4FA),
    secondary = Gold,
    onSecondary = Color(0xFF3A2803),
    secondaryContainer = GoldDark,
    onSecondaryContainer = Color(0xFFFFE8B0),
    tertiary = GoldLight,
    background = NavyDeep,
    onBackground = Color(0xFFE8EAF2),
    surface = Color(0xFF0C1A3C),
    onSurface = Color(0xFFE8EAF2),
    surfaceVariant = Color(0xFF1B2951),
    onSurfaceVariant = Color(0xFFB4BCD4),
    surfaceContainerLowest = Color(0xFF040B1F),
    surfaceContainerLow = Color(0xFF0A1633),
    surfaceContainer = Color(0xFF0E1D42),
    surfaceContainerHigh = Color(0xFF14264F),
    surfaceContainerHighest = Color(0xFF1B305E),
    outline = Color(0xFF8E97B2),
    outlineVariant = Color(0xFF2E3C66),
    error = Color(0xFFF2B8B5)
)

@Composable
fun FutSoccerBrasilTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val darkTheme = themeMode == ThemeMode.DARK
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.findActivity() ?: return@SideEffect).window
            window.statusBarColor = if (darkTheme) NavyDeep.toArgb() else NavyPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

private fun android.view.View.findActivity(): Activity? =
    generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
        .filterIsInstance<Activity>()
        .firstOrNull()
