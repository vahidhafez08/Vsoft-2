package com.vsoft.app.ui.screens.more

import androidx.lifecycle.ViewModel
import com.vsoft.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val userName: StateFlow<String> = repo.userName
    val themeMode: StateFlow<Int> = repo.themeMode
    val notifications: StateFlow<Boolean> = repo.notifications

    fun setUserName(name: String) = repo.setUserName(name)
    fun setThemeMode(mode: Int) = repo.setThemeMode(mode)
    fun setNotifications(enabled: Boolean) = repo.setNotifications(enabled)
    fun resetSettings() = repo.resetSettings()
}
