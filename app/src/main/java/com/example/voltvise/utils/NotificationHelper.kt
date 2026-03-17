package com.example.voltvise.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.voltvise.R
import com.example.voltvise.activities.MainActivity

object NotificationHelper {

    private const val CHANNEL_REMINDER_ID   = "meter_reminder"
    private const val CHANNEL_BILL_ALERT_ID = "bill_alert"

    // Call this once when app starts (in MainActivity.onCreate)
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // Daily reminder channel
            NotificationChannel(
                CHANNEL_REMINDER_ID,
                "Meter Reading Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to enter your meter reading"
                manager.createNotificationChannel(this)
            }

            // Bill alert channel
            NotificationChannel(
                CHANNEL_BILL_ALERT_ID,
                "Bill Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when your predicted bill is high"
                manager.createNotificationChannel(this)
            }
        }
    }

    // Daily reminder notification
    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("⚡ VoltVise Reminder")
            .setContentText("Don't forget to enter your meter reading today!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Track your electricity usage daily for accurate bill prediction. Tap to open VoltVise."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }
    }

    // High bill alert notification
    fun showBillAlert(context: Context, predictedBill: Int, threshold: Int) {
        if (predictedBill < threshold) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BILL_ALERT_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ High Bill Alert")
            .setContentText("Your predicted bill is ₹$predictedBill this month!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your predicted bill ₹$predictedBill exceeds your alert limit ₹$threshold. Consider reducing usage. Tap to view details."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(1002, notification)
        }
    }
}