package com.vsoft.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                try {
                    ReminderScheduler.schedule(context.applicationContext)
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Could not schedule task reminder", e)
                }
            }
        }
    }
}
