package com.example.localjobs.pref

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_preferences")

class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
    }

    // Get preferences
    val preferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val theme = preferences[THEME_KEY] ?: "System Default"
        val language = preferences[LANGUAGE_KEY] ?: "English"
        val notifications = preferences[NOTIFICATIONS_KEY] ?: true
        UserPreferences(theme, language, notifications)
    }

    // Save preferences
    suspend fun updateTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }
}

data class UserPreferences(
    val theme: String,
    val language: String,
    val notificationsEnabled: Boolean
)
