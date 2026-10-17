package com.vsoft.app

import android.app.Application
import android.util.Log
import com.vsoft.app.data.db.AppDatabase
import com.vsoft.app.notify.NotificationHelper
import com.vsoft.app.notify.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VsoftApp : Application() {

    @Inject lateinit var database: AppDatabase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.ensureChannel(this)
            ReminderScheduler.schedule(this)
        } catch (e: Exception) {
            Log.e("VsoftApp", "Setting up task reminders failed", e)
        }
        // The app no longer seeds demo data. Wipe the old demo data exactly once.
        appScope.launch {
            val prefs = getSharedPreferences("vsoft_prefs", MODE_PRIVATE)
            if (!prefs.getBoolean("demo_cleared", false)) {
                runCatching { database.clearAllTables() }
                    .onSuccess { prefs.edit().putBoolean("demo_cleared", true).apply() }
                    .onFailure { Log.e("VsoftApp", "Clearing demo data failed", it) }
            }
        }
    }
}