package com.example.localjobs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.example.localjobs.pref.PreferencesManager
import com.example.localjobs.pref.SettingPreferences
import com.example.localjobs.screen.artisan.HomeArt
import com.example.localjobs.screen.splashScreen
import com.example.localjobs.screen.user.HomeScreen
import com.google.firebase.auth.FirebaseAuth
import org.koin.java.KoinJavaComponent.inject as koinInject


class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by koinInject(FirebaseAuth::class.java)
    private val preferencesManager: PreferencesManager by koinInject(PreferencesManager::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by preferencesManager.preferencesFlow.collectAsState(
                initial = SettingPreferences("System Default", "English", true, "Guest", "", "")
            )

            val darkTheme = when (preferences.theme) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                val user = auth.currentUser
                val isLoggedIn = preferencesManager.isUserLoggedIn.collectAsState(initial = false).value

                // 🔹 Correctly Retrieve User Role
                val userRole by preferencesManager.userRole.collectAsState(initial = "Users")

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigator(
                        screen = when {
                            user != null -> {
                                when (userRole) {
                                    "Artisans" -> HomeArt() // Technician home screen
                                    else -> HomeScreen() // Default to user home screen
                                }
                            }
                            isLoggedIn -> {
                                when (userRole) {
                                    "Artisans" -> HomeArt()
                                    else -> HomeScreen()
                                }
                            }
                            else -> splashScreen() // Login/registration flow
                        }
                    )
                    Modifier.padding(innerPadding)
                }
            }
        }
    }
}
