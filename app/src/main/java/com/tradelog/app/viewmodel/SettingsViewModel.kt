package com.tradelog.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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

    fun saveSettings(updated: UserSettings) {
        viewModelScope.launch {
            Log.d(TAG, "saveSettings() — displayName=${updated.displayName} lang=${updated.language}")
            settingsRepository.saveSettings(updated)
            // Apply the selected locale immediately — Activity recreates with new language (FR-03)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(updated.language))
            Log.i(TAG, "Settings saved — locale set to ${updated.language}")
        }
    }

    fun addInstrument(value: String) {
        val current = settings.value
        if (value.isBlank() || current.instruments.contains(value)) return
        viewModelScope.launch { settingsRepository.saveSettings(current.copy(instruments = current.instruments + value.uppercase())) }
    }

    fun removeInstrument(value: String) {
        val current = settings.value
        viewModelScope.launch { settingsRepository.saveSettings(current.copy(instruments = current.instruments - value)) }
    }

    fun addStrategy(value: String) {
        val current = settings.value
        if (value.isBlank() || current.strategies.contains(value)) return
        viewModelScope.launch { settingsRepository.saveSettings(current.copy(strategies = current.strategies + value)) }
    }

    fun removeStrategy(value: String) {
        val current = settings.value
        viewModelScope.launch { settingsRepository.saveSettings(current.copy(strategies = current.strategies - value)) }
    }

    fun addSession(value: String) {
        val current = settings.value
        if (value.isBlank() || current.sessions.contains(value)) return
        viewModelScope.launch { settingsRepository.saveSettings(current.copy(sessions = current.sessions + value.uppercase())) }
    }

    fun removeSession(value: String) {
        val current = settings.value
        viewModelScope.launch { settingsRepository.saveSettings(current.copy(sessions = current.sessions - value)) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "SettingsViewModel cleared")
    }
}
