package com.example.localjobs.Screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.localjobs.screen.RegisterLoginScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Locale

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current

        val preferencesManager: PreferencesManager = koinInject()
        val firebaseAuth: FirebaseAuth = koinInject()

        val preferences by preferencesManager.preferencesFlow.collectAsState(
            initial = SettingPreferences("System Default", "English", true , "Guest", "", "")
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
                    updateLocale(context, language)
                    restartActivity(context)
                }
            },
            onNotificationsToggle = { enabled ->
                coroutineScope.launch {
                    preferencesManager.updateNotifications(enabled)
                }
            },
            onLogout = {
                coroutineScope.launch {
                    // Sign out from Firebase
                    firebaseAuth.signOut()
                    // Clear login state in PreferencesManager
                    preferencesManager.clearPreferences()
                    // Navigate to the login screen
                    navigator?.replace(RegisterLoginScreen())
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
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }

    private fun restartActivity(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }
}

@Composable
fun SettingsContent(
    settingPreferences: SettingPreferences,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Theme Selection with Dynamic Icon
        val themeIcon = when (settingPreferences.theme) {
            "Light" -> Icons.Filled.LightMode
            "Dark" -> Icons.Filled.DarkMode
            else -> Icons.Filled.Settings
        }

        SettingsOption(
            icon = themeIcon,
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
            icon = Icons.Filled.Language,
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
                imageVector = Icons.Filled.Notifications,
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
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Logout")
        }

        // GitHub Icon with Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = "GitHub",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mohammedzineddine"))
                        context.startActivity(intent)
                    }
            )
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