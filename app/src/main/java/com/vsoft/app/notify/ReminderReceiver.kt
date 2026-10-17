package com.vsoft.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.vsoft.app.data.db.AppDatabase
import com.vsoft.app.util.startOfDay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderDatabaseEntryPoint {
    fun appDatabase(): AppDatabase
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val prefs = appContext.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE)
                if (!prefs.getBoolean("notifications_enabled", true)) return@launch

                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    ReminderDatabaseEntryPoint::class.java
                )
                val database = entryPoint.appDatabase()
                val todayStart = startOfDay(System.currentTimeMillis())
                val endCalendar = Calendar.getInstance().apply {
                    timeInMillis = todayStart
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                val count = database.taskDao().countDueUntil(endCalendar.timeInMillis)
                if (count > 0) {
                    NotificationHelper.ensureChannel(appContext)
                    NotificationHelper.showTaskReminder(appContext, count)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "failed", e)
            } finally {
                try {
                    ReminderScheduler.schedule(appContext)
                } catch (e: Exception) {
                    Log.e("ReminderReceiver", "rescheduling failed", e)
                }
                pending.finish()
            }
        }
    }
}
