package com.example.localjobs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.localjobs.pref.PreferencesManager
import com.example.localjobs.pref.SettingPreferences
import com.google.firebase.auth.FirebaseAuth
import cafe.adriel.voyager.navigator.Navigator
import com.example.localjobs.Screens.SplashScreen
import com.example.localjobs.Screens.user.HomeScreen
import org.koin.java.KoinJavaComponent.inject as koinInject

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by koinInject(FirebaseAuth::class.java)
    private val preferencesManager: PreferencesManager by koinInject(PreferencesManager::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesFlow = preferencesManager.preferencesFlow

        setContent {
            val preferences by preferencesFlow.collectAsState(initial = SettingPreferences("System Default", "English", true))

            val darkTheme = when (preferences.theme) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                val user = auth.currentUser

                // Check if the user is logged in from preferences
                val isLoggedIn = preferencesManager.isUserLoggedIn.collectAsState(initial = false).value

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigator(
                        screen = when {
                            user != null -> {
                                // If the Firebase user is logged in, navigate to the Home Screen
                                HomeScreen()
                            }
                            isLoggedIn -> {
                                // If the user is logged in in Preferences, navigate to Home
                                HomeScreen()
                            }
                            else -> {
                                // If the user is not logged in, navigate to the Login Screen
                                SplashScreen()
                            }
                        }
                    )
                    Modifier.padding(innerPadding)
                }
            }
        }
    }
}
