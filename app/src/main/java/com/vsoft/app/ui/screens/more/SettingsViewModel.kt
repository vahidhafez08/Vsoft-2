package com.vsoft.app.ui.screens.more

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.vsoft.app.data.backup.BackupManager
import com.vsoft.app.data.repository.SettingsRepository
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val userName: StateFlow<String> = repo.userName
    val themeMode: StateFlow<Int> = repo.themeMode
    val notifications: StateFlow<Boolean> = repo.notifications

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun setUserName(name: String) = repo.setUserName(name)
    fun setThemeMode(mode: Int) = repo.setThemeMode(mode)
    fun setNotifications(enabled: Boolean) = repo.setNotifications(enabled)
    fun resetSettings() = repo.resetSettings()

    fun backupTo(uri: Uri) {
        if (_busy.value) return
        _busy.value = true
        safeLaunch {
            try {
                val json = backupManager.exportJson()
                val result = backupManager.writeText(uri, json)
                if (result.isSuccess) {
                    UiEvents.show("بکاپ با موفقیت ذخیره شد ✅")
                } else {
                    UiEvents.show("ذخیره‌ی بکاپ ناموفق بود")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                UiEvents.show("ذخیره‌ی بکاپ ناموفق بود")
            } finally {
                _busy.value = false
            }
        }
    }

    fun restoreFrom(uri: Uri) {
        if (_busy.value) return
        _busy.value = true
        safeLaunch {
            try {
                val textResult = backupManager.readText(uri)
                if (textResult.isFailure) {
                    UiEvents.show("فایل بکاپ معتبر نیست یا خراب است")
                    return@safeLaunch
                }
                val restoreResult = backupManager.importJson(textResult.getOrThrow())
                if (restoreResult.isSuccess) {
                    UiEvents.show("اطلاعات با موفقیت بازیابی شد ✅")
                } else {
                    UiEvents.show("فایل بکاپ معتبر نیست یا خراب است")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                UiEvents.show("فایل بکاپ معتبر نیست یا خراب است")
            } finally {
                _busy.value = false
            }
        }
    }

    fun exportCsvTo(uri: Uri) {
        if (_busy.value) return
        _busy.value = true
        safeLaunch {
            try {
                val csv = backupManager.exportTransactionsCsv()
                val result = backupManager.writeText(uri, csv)
                if (result.isSuccess) {
                    UiEvents.show("خروجی CSV ذخیره شد ✅")
                } else {
                    UiEvents.show("ذخیره‌ی فایل ناموفق بود")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                UiEvents.show("ذخیره‌ی فایل ناموفق بود")
            } finally {
                _busy.value = false
            }
        }
    }
}
