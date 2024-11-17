package com.example.localjobs.Screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.localjobs.pref.PreferencesManager
import com.example.localjobs.pref.UserPreferences
import kotlinx.coroutines.launch
import java.util.*

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        val preferencesManager = PreferencesManager(context)
        val preferences by preferencesManager.preferencesFlow.collectAsState(
            initial = UserPreferences("System Default", "English", true)
        )

        val coroutineScope = rememberCoroutineScope()

        SettingsContent(
            userPreferences = preferences,
            onThemeChange = { theme ->
                coroutineScope.launch {
                    preferencesManager.updateTheme(theme)
                }
            },
            onLanguageChange = { language ->
                coroutineScope.launch {
                    preferencesManager.updateLanguage(language)
                    updateLocale(context, language) // Change locale
                }
            },
            onNotificationsToggle = { enabled ->
                coroutineScope.launch {
                    preferencesManager.updateNotifications(enabled)
                }
            }
        )
    }

    private fun updateLocale(context: Context, language: String) {
        val locale = when (language) {
            "Arabic" -> Locale("ar")
            "French" -> Locale("fr")
            else -> Locale("en")
        }

        val config = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

@Composable
fun SettingsContent(
    userPreferences: UserPreferences,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Theme Selection
        SettingsOption(
            icon = Icons.Outlined.Menu,
            title = "Theme",
            value = userPreferences.theme,
            onClick = {
                val newTheme = when (userPreferences.theme) {
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
            value = userPreferences.language,
            onClick = {
                val newLanguage = when (userPreferences.language) {
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
                checked = userPreferences.notificationsEnabled,
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
