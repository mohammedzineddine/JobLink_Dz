package com.example.localjobs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.localjobs.pref.PreferencesManager
import com.example.localjobs.pref.UserPreferences
import cafe.adriel.voyager.navigator.Navigator
import com.example.localjobs.Screens.SplashScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(applicationContext)
        val preferencesFlow = preferencesManager.preferencesFlow

        setContent {
            // Observe the selected theme preference and set the theme accordingly
            val preferences by preferencesFlow.collectAsState(initial = UserPreferences("System Default", "English", true))

            val darkTheme = when (preferences.theme) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme() // System Default
            }

            // Apply the selected theme dynamically
            MaterialTheme(
                colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigator(SplashScreen())
                    Modifier.padding(innerPadding) // To respect the inner padding of Scaffold
                }
            }
        }
    }
}
