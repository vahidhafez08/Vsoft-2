package com.vsoft.app

import android.app.Application
import android.util.Log
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.data.repository.TaskRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VsoftApp : Application() {

    @Inject lateinit var financeRepository: FinanceRepository
    @Inject lateinit var taskRepository: TaskRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            val prefs = getSharedPreferences("vsoft_prefs", MODE_PRIVATE)
            runCatching { financeRepository.seedDemoDataIfNeeded(prefs) }
                .onFailure { Log.e("VsoftApp", "Finance seeding failed", it) }
            runCatching { taskRepository.seedDemoDataIfNeeded(prefs) }
                .onFailure { Log.e("VsoftApp", "Task seeding failed", it) }
        }
    }
}