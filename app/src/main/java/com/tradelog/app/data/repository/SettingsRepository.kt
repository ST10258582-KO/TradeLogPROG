package com.tradelog.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tradelog.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Extension property to create a single DataStore instance per application lifecycle.
// Reference: Google LLC, "DataStore" Android Developers (2024)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tradelog_settings")

// SettingsRepository persists user preferences locally via DataStore and syncs to the REST API.
// Local storage ensures settings are available immediately (FR-03).
class SettingsRepository(private val context: Context) {

    private val TAG = "SettingsRepository"

    // Preference keys
    companion object {
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_TIMEZONE = stringPreferencesKey("timezone")
        val KEY_DEFAULT_RISK = floatPreferencesKey("default_risk")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications")
    }

    // Observe settings as a Flow — UI collects and reacts to changes automatically
    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { e ->
            Log.e(TAG, "DataStore read error: ${e.message}", e)
            emit(emptyPreferences())
        }
        .map { prefs ->
            UserSettings(
                displayName = prefs[KEY_DISPLAY_NAME] ?: "Trader",
                timezone = prefs[KEY_TIMEZONE] ?: "Africa/Johannesburg",
                defaultRisk = prefs[KEY_DEFAULT_RISK] ?: 1.0f,
                currency = prefs[KEY_CURRENCY] ?: "USD",
                language = prefs[KEY_LANGUAGE] ?: "en",
                notifications = prefs[KEY_NOTIFICATIONS] ?: true
            )
        }

    // Persist settings locally and sync to API
    suspend fun saveSettings(settings: UserSettings) {
        try {
            Log.d(TAG, "Saving settings for: ${settings.displayName}")
            context.dataStore.edit { prefs ->
                prefs[KEY_DISPLAY_NAME] = settings.displayName
                prefs[KEY_TIMEZONE] = settings.timezone
                prefs[KEY_DEFAULT_RISK] = settings.defaultRisk
                prefs[KEY_CURRENCY] = settings.currency
                prefs[KEY_LANGUAGE] = settings.language
                prefs[KEY_NOTIFICATIONS] = settings.notifications
            }
            // Sync to REST API (FR-03)
            val response = RetrofitClient.apiService.updateSettings(
                mapOf(
                    "displayName" to settings.displayName,
                    "timezone" to settings.timezone,
                    "defaultRisk" to settings.defaultRisk,
                    "currency" to settings.currency,
                    "language" to settings.language,
                    "notifications" to settings.notifications
                )
            )
            if (response.isSuccessful) {
                Log.i(TAG, "Settings synced to API")
            } else {
                Log.w(TAG, "Settings API sync failed: HTTP ${response.code()} — local save retained")
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveSettings error: ${e.message}", e)
        }
    }
}

// Simple value object for settings — mirrors FR-03 fields
data class UserSettings(
    val displayName: String = "Trader",
    val timezone: String = "Africa/Johannesburg",
    val defaultRisk: Float = 1.0f,
    val currency: String = "USD",
    val language: String = "en",
    val notifications: Boolean = true
)
