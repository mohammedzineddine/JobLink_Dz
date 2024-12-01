package com.example.localjobs.Screens

import UserLoginScreen
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.localjobs.pref.PreferencesManager
import com.example.localjobs.pref.SettingPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*
import org.koin.compose.koinInject


class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current

        // Use Koin to inject PreferencesManager and FirebaseAuth
        val preferencesManager: PreferencesManager = koinInject()
        val firebaseAuth: FirebaseAuth = koinInject()

        val preferences by preferencesManager.preferencesFlow.collectAsState(
            initial = SettingPreferences("System Default", "English", true)
        )

        val coroutineScope = rememberCoroutineScope()

        SettingsContent(
            settingPreferences = preferences,
            onThemeChange = { theme ->
                coroutineScope.launch {
                    preferencesManager.updateTheme(theme)
                }
            },
            onLanguageChange = { language ->
                coroutineScope.launch {
                    preferencesManager.updateLanguage(language)
                    updateLocale(context, language) // Change locale
                    restartActivity(context) // Restart activity to apply changes
                }
            },
            onNotificationsToggle = { enabled ->
                coroutineScope.launch {
                    preferencesManager.updateNotifications(enabled)
                }
            },
            onLogout = {
                // Sign out the user
                firebaseAuth.signOut()
                // Navigate to login screen after logout
                navigator?.push(IntroScreen()) // Assuming you have UserLoginScreen
            }
        )
    }

    private fun updateLocale(context: Context, language: String) {
        val locale = when (language) {
            "Arabic" -> Locale("values-ar-DZ")
            "French" -> Locale("values-fr-FR")
            else -> Locale("en") // Default to English
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Update the locale using createConfigurationContext
        context.createConfigurationContext(config)
    }

    private fun restartActivity(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}

@Composable
fun SettingsContent(
    settingPreferences: SettingPreferences,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onLogout: () -> Unit // New logout callback
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Theme Selection
        SettingsOption(
            icon = Icons.Outlined.Menu,
            title = "Theme",
            value = settingPreferences.theme,
            onClick = {
                val newTheme = when (settingPreferences.theme) {
                    "Light" -> "Dark"
                    "Dark" -> "System Default"
                    else -> "Light"
                }
                onThemeChange(newTheme)
            }
        )

        // Language Selection
        SettingsOption(
            icon = Icons.Outlined.Info,
            title = "Language",
            value = settingPreferences.language,
            onClick = {
                val newLanguage = when (settingPreferences.language) {
                    "English" -> "Arabic"
                    "Arabic" -> "French"
                    else -> "English"
                }
                onLanguageChange(newLanguage)
            }
        )

        // Notifications Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Notifications",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = settingPreferences.notificationsEnabled,
                onCheckedChange = onNotificationsToggle
            )
        }

        // Account Management
        Button(
            onClick = { /* Navigate to Account Management */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Manage Account")
        }

        // Logout Button
        Button(
            onClick = onLogout, // Logout logic
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun SettingsOption(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 18.sp)
            Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
