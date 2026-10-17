package com.vsoft.app.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vsoft.app.MainActivity
import com.vsoft.app.R
import com.vsoft.app.util.toPersianDigits

object NotificationHelper {
    const val CHANNEL_ID = "task_reminders"

    fun ensureChannel(context: Context) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآوری کارها",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "یادآوری روزانه‌ی کارهای امروز و عقب‌افتاده"
            }
            manager.createNotificationChannel(channel)
        } catch (_: Exception) {
            // Notifications must never prevent the app from opening.
        }
    }

    fun canPost(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun showTaskReminder(context: Context, count: Int) {
        if (count <= 0 || !canPost(context)) return
        try {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                2002,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val message = "${count.toString().toPersianDigits()} کار برای امروز یا عقب‌افتاده داری"
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("یادآوری VSOFT")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {
            // Permission may have been revoked between the check and notify.
        } catch (_: Exception) {
            // Notification delivery is best effort.
        }
    }
}
