package com.vsoft.app.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** App settings kept in SharedPreferences "vsoft_prefs". themeMode: 0 = system, 1 = light, 2 = dark. */
@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(readName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME, 0).coerceIn(0, 2))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _notifications = MutableStateFlow(prefs.getBoolean(KEY_NOTIF, true))
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()

    private fun readName(): String =
        prefs.getString(KEY_NAME, null)?.trim().orEmpty().let { savedName ->
            if (savedName.isBlank() || savedName == "وحید") DEFAULT_NAME else savedName
        }

    fun setUserName(value: String) {
        val v = value.trim().take(24)
        if (v.isEmpty()) return
        prefs.edit().putString(KEY_NAME, v).apply()
        _userName.value = v
    }

    fun setThemeMode(mode: Int) {
        val m = mode.coerceIn(0, 2)
        prefs.edit().putInt(KEY_THEME, m).apply()
        _themeMode.value = m
    }

    fun setNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF, enabled).apply()
        _notifications.value = enabled
    }

    /** Resets only name, theme and notifications. Financial data and tasks are untouched. */
    fun resetSettings() {
        prefs.edit()
            .remove(KEY_NAME)
            .remove(KEY_THEME)
            .remove(KEY_NOTIF)
            .apply()
        _userName.value = DEFAULT_NAME
        _themeMode.value = 0
        _notifications.value = true
    }

    companion object {
        const val DEFAULT_NAME = "کاربر"
        private const val KEY_NAME = "user_name"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_NOTIF = "notifications_enabled"
    }
}
