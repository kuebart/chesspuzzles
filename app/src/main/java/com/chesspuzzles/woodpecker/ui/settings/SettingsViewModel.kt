package com.chesspuzzles.woodpecker.ui.settings

import androidx.lifecycle.ViewModel
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _language = MutableStateFlow(appPreferences.getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _soundEnabled = MutableStateFlow(appPreferences.isSoundEnabled())
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _autoAdvance = MutableStateFlow(appPreferences.isAutoAdvance())
    val autoAdvance: StateFlow<Boolean> = _autoAdvance.asStateFlow()

    fun setLanguage(lang: String) {
        appPreferences.setLanguage(lang)
        _language.value = lang
    }

    fun toggleSound() {
        val newValue = !_soundEnabled.value
        appPreferences.setSoundEnabled(newValue)
        _soundEnabled.value = newValue
    }

    fun toggleAutoAdvance() {
        val newValue = !_autoAdvance.value
        appPreferences.setAutoAdvance(newValue)
        _autoAdvance.value = newValue
    }
}
