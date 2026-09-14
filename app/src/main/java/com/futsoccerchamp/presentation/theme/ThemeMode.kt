package com.futsoccerchamp.presentation.theme

import android.content.Context
import android.content.res.Configuration

enum class ThemeMode(val label: String) {
    LIGHT("Modo claro"),
    DARK("Modo escuro");

    fun toggled(): ThemeMode = if (this == LIGHT) DARK else LIGHT
}

class ThemePreferences(private val context: Context) {

    private val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    fun load(): ThemeMode {
        val stored = preferences.getString(KEY, null) ?: return systemDefault()
        return runCatching { ThemeMode.valueOf(stored) }.getOrElse { systemDefault() }
    }

    fun save(mode: ThemeMode) {
        preferences.edit().putString(KEY, mode.name).apply()
    }

    private fun systemDefault(): ThemeMode {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) ThemeMode.DARK else ThemeMode.LIGHT
    }

    private companion object {
        const val KEY = "theme_mode"
    }
}
