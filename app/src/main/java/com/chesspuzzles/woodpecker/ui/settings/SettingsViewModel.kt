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

    fun setLanguage(lang: String) {
        appPreferences.setLanguage(lang)
        _language.value = lang
    }
}
