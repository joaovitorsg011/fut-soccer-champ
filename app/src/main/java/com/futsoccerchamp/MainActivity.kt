package com.futsoccerchamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.futsoccerchamp.presentation.AppNavigation
import com.futsoccerchamp.presentation.common.FirebaseSetupScreen
import com.futsoccerchamp.presentation.theme.FutSoccerChampTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseReady = FirebaseApp.getApps(this).isNotEmpty()

        setContent {
            FutSoccerChampTheme {
                if (firebaseReady) AppNavigation() else FirebaseSetupScreen()
            }
        }
    }
}
