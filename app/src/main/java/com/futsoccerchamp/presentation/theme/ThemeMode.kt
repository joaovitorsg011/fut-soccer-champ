package com.futsoccerchamp.presentation.theme

import android.content.Context

enum class ThemeMode(val label: String) {
    SYSTEM("Tema do sistema"),
    LIGHT("Modo claro"),
    DARK("Modo escuro");

    fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]
}

class ThemePreferences(context: Context) {

    private val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    fun load(): ThemeMode = runCatching {
        ThemeMode.valueOf(preferences.getString(KEY, ThemeMode.SYSTEM.name).orEmpty())
    }.getOrDefault(ThemeMode.SYSTEM)

    fun save(mode: ThemeMode) {
        preferences.edit().putString(KEY, mode.name).apply()
    }

    private companion object {
        const val KEY = "theme_mode"
    }
}
