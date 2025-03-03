package com.example.localjobs.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore extension for accessing preferences
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role") // Key for user role
    }

    // Flow to get preferences
    val preferencesFlow: Flow<SettingPreferences> = dataStore.data.map { preferences ->
        SettingPreferences(
            theme = preferences[THEME_KEY] ?: "System Default",
            language = preferences[LANGUAGE_KEY] ?: "English",
            notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true,
            userName = preferences[USER_NAME_KEY] ?: "Guest",
            userId = preferences[USER_ID_KEY] ?: "",
            userRole = preferences[USER_ROLE_KEY] ?: "" // Default to empty string instead of "artisan"
        )
    }

    // Save user preferences
    suspend fun updateTheme(theme: String) {
        dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { it[LANGUAGE_KEY] = language }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_KEY] = enabled }
    }

    // Save user role
    suspend fun updateUserRole(userRole: String) {
        dataStore.edit { it[USER_ROLE_KEY] = userRole }
    }

    // Clear all preferences (e.g., on logout)
    suspend fun clearPreferences() {
        dataStore.edit { it.clear() }
    }

    // Save login state and user details
    suspend fun saveLoginState(isLoggedIn: Boolean, userId: String?, userRole: String) {
        dataStore.edit {
            it[IS_LOGGED_IN_KEY] = isLoggedIn
            it[USER_ID_KEY] = userId ?: ""
            it[USER_ROLE_KEY] = userRole // Save user role persistently
        }
    }

    // Get user role immediately (not a Flow)
    suspend fun getUserRole(): String {
        val preferences = dataStore.data.first() // Reads once synchronously
        return preferences[USER_ROLE_KEY] ?: "" // Return empty string if no role is found
    }

    // Get login state as a Flow
    val isUserLoggedIn: Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }

    // Get user ID as a Flow
    val userId: Flow<String> = dataStore.data.map { it[USER_ID_KEY] ?: "" }

    // Get user role as a Flow
    val userRole: Flow<String> = dataStore.data.map { it[USER_ROLE_KEY] ?: "" } // Default to empty string
}

// Data class to hold preferences
data class SettingPreferences(
    val theme: String,
    val language: String,
    val notificationsEnabled: Boolean,
    val userName: String,
    val userId: String,
    val userRole: String // User role is included here
)