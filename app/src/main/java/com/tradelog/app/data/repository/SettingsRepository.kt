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
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tradelog_settings")

// SettingsRepository persists user preferences locally via DataStore and syncs to the REST API.
// Local storage ensures settings are available immediately (FR-03).
class SettingsRepository(private val context: Context) {

    private val TAG = "SettingsRepository"

    companion object {
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_TIMEZONE = stringPreferencesKey("timezone")
        val KEY_DEFAULT_RISK = floatPreferencesKey("default_risk")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications")
        val KEY_INSTRUMENTS = stringPreferencesKey("instruments")
        val KEY_STRATEGIES = stringPreferencesKey("strategies")
        val KEY_SESSIONS = stringPreferencesKey("sessions")

        val DEFAULT_INSTRUMENTS = listOf("NQ", "ES", "GC", "US30m", "UK100m", "USTECm", "EURUSD", "GBPUSD")
        val DEFAULT_STRATEGIES = listOf("ORB", "Breakout", "Trend Pullback", "Mean Reversion", "News Trade", "Scalp")
        val DEFAULT_SESSIONS = listOf("LONDON", "NEW_YORK", "ASIA", "OTHER")
    }

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
                notifications = prefs[KEY_NOTIFICATIONS] ?: true,
                instruments = prefs[KEY_INSTRUMENTS]?.split(",")?.filter { it.isNotBlank() } ?: DEFAULT_INSTRUMENTS,
                strategies = prefs[KEY_STRATEGIES]?.split(",")?.filter { it.isNotBlank() } ?: DEFAULT_STRATEGIES,
                sessions = prefs[KEY_SESSIONS]?.split(",")?.filter { it.isNotBlank() } ?: DEFAULT_SESSIONS
            )
        }

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
                prefs[KEY_INSTRUMENTS] = settings.instruments.joinToString(",")
                prefs[KEY_STRATEGIES] = settings.strategies.joinToString(",")
                prefs[KEY_SESSIONS] = settings.sessions.joinToString(",")
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
    val notifications: Boolean = true,
    val instruments: List<String> = SettingsRepository.DEFAULT_INSTRUMENTS,
    val strategies: List<String> = SettingsRepository.DEFAULT_STRATEGIES,
    val sessions: List<String> = SettingsRepository.DEFAULT_SESSIONS
)
