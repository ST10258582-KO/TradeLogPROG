package com.tradelog.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tradelog.app.data.repository.SettingsRepository
import com.tradelog.app.data.repository.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// SettingsViewModel drives the Settings screen (FR-03).
// Uses AndroidViewModel to access Application context needed by DataStore.
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "SettingsViewModel"
    private val settingsRepository = SettingsRepository(application)

    // Exposes settings as a hot StateFlow — UI observes this to render current values
    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSettings()
        )

    // Save updated settings — persists locally and syncs to API (FR-03)
    fun saveSettings(updated: UserSettings) {
        viewModelScope.launch {
            Log.d(TAG, "saveSettings() — displayName=${updated.displayName} lang=${updated.language}")
            settingsRepository.saveSettings(updated)
            Log.i(TAG, "Settings saved successfully")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "SettingsViewModel cleared")
    }
}
