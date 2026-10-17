package com.vsoft.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    private const val PREFS_NAME = "vsoft_prefs"
    private const val KEY_NOTIFICATIONS = "notifications_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val REQUEST_CODE = 2001
    private const val DEFAULT_HOUR = 8

    fun getHour(context: Context): Int = try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_REMINDER_HOUR, DEFAULT_HOUR).coerceIn(0, 23)
    } catch (_: Exception) {
        DEFAULT_HOUR
    }

    fun setHour(context: Context, hour: Int) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_REMINDER_HOUR, hour.coerceIn(0, 23)).apply()
        } catch (_: Exception) {
            // A failed preference write must not crash the settings screen.
        }
    }

    fun schedule(context: Context) {
        try {
            cancel(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_NOTIFICATIONS, true)) return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerMillis(System.currentTimeMillis(), getHour(context)),
                pendingIntent
            )
        } catch (_: Exception) {
            // Scheduling is best effort; the app can retry on its next launch.
        }
    }

    fun cancel(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (_: Exception) {
            // A missing alarm service or pending intent is harmless.
        }
    }

    internal fun nextTriggerMillis(nowMillis: Long, hour: Int): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val trigger = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.timeInMillis - nowMillis <= 60_000L) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }
}
