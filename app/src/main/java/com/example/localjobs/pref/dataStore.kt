package com.example.localjobs.pref

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore

// DataStore extension for accessing preferences
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    // Access to the data store
    private val dataStore: DataStore<Preferences> = context.dataStore

    // Preference keys
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_NAME_KEY = stringPreferencesKey("user_name") // New key for user's name
    }

    // Flow to get all preferences
    val preferencesFlow: Flow<SettingPreferences> = dataStore.data.map { preferences ->
        val theme = preferences[THEME_KEY] ?: "System Default"
        val language = preferences[LANGUAGE_KEY] ?: "English"
        val notifications = preferences[NOTIFICATIONS_KEY] ?: true
        val userName = preferences[USER_NAME_KEY] ?: "Guest" // Default name
        SettingPreferences(theme, language, notifications, userName)
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

    // Get login state
    val isUserLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

}

// Data class to hold user preferences
data class SettingPreferences(
    val theme: String,
    val language: String,
    val notificationsEnabled: Boolean,
    val userName: String,
) {
    constructor(theme: String, language: String, notificationsEnabled: Boolean) : this(
        theme = theme,
        language = language,
        notificationsEnabled = notificationsEnabled,
        userName = "Guest"
    )


}
