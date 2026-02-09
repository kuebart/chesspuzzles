package com.chesspuzzles.woodpecker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "de") ?: "de"

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun shouldShowInfoBox(): Boolean = prefs.getBoolean(KEY_SHOW_INFO_BOX, true)

    fun setInfoBoxDismissed() {
        prefs.edit().putBoolean(KEY_SHOW_INFO_BOX, false).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isAutoAdvance(): Boolean = prefs.getBoolean(KEY_AUTO_ADVANCE, false)

    fun setAutoAdvance(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ADVANCE, enabled).apply()
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_SHOW_INFO_BOX = "show_info_box"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_AUTO_ADVANCE = "auto_advance"
    }
}
