package com.futsoccerchamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.futsoccerchamp.presentation.AppNavigation
import com.futsoccerchamp.presentation.common.FirebaseSetupScreen
import com.futsoccerchamp.presentation.theme.FutSoccerChampTheme
import com.futsoccerchamp.presentation.theme.ThemeMode
import com.futsoccerchamp.presentation.theme.ThemePreferences
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseReady = FirebaseApp.getApps(this).isNotEmpty()
        val themePreferences = ThemePreferences(this)

        setContent {
            var themeMode by remember { mutableStateOf(themePreferences.load()) }

            FutSoccerChampTheme(themeMode = themeMode) {
                if (firebaseReady) {
                    AppNavigation(
                        themeMode = themeMode,
                        onThemeModeChange = { mode ->
                            themeMode = mode
                            themePreferences.save(mode)
                        }
                    )
                } else {
                    FirebaseSetupScreen()
                }
            }
        }
    }
}
