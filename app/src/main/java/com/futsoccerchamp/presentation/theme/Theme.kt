package com.futsoccerchamp.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Green = Color(0xFF0B6B3A)
private val GreenLight = Color(0xFF3E9C63)
private val Sand = Color(0xFFF5F3EE)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF2F6F4F),
    background = Sand,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color.Black,
    primaryContainer = Green,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF7FBF9A)
)

@Composable
fun FutSoccerChampTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.findActivity() ?: return@SideEffect).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

private fun android.view.View.findActivity(): Activity? =
    generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
        .filterIsInstance<Activity>()
        .firstOrNull()
