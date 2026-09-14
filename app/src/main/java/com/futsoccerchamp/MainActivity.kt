package com.futsoccerchamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.futsoccerchamp.presentation.AppNavigation
import com.futsoccerchamp.presentation.theme.FutSoccerChampTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FutSoccerChampTheme {
                AppNavigation()
            }
        }
    }
}
