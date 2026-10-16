package com.vsoft.app

import android.app.Application
import android.util.Log
import com.vsoft.app.data.repository.FinanceRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VsoftApp : Application() {

    @Inject lateinit var repository: FinanceRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            try {
                repository.seedDemoDataIfNeeded(
                    getSharedPreferences("vsoft_prefs", MODE_PRIVATE)
                )
            } catch (e: Exception) {
                Log.e("VsoftApp", "Seeding failed", e)
            }
        }
    }
}