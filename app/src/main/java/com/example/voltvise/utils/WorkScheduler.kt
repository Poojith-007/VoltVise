package com.example.voltvise.utils

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleDailyReminder(context: Context) {
        val prefs = context.getSharedPreferences("voltvise_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("reminder_enabled", true)
        if (!enabled) return

        val hourOfDay = prefs.getInt("reminder_hour", 20)
        val minute    = prefs.getInt("reminder_minute", 0)

        val now    = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today → schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delayMs = target.timeInMillis - now.timeInMillis

        // ✅ Use PERIODIC so it repeats every 24 hours
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun scheduleBillAlertCheck(context: Context) {
        val prefs   = context.getSharedPreferences("voltvise_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("bill_alert_enabled", true)
        if (!enabled) return

        val billAlertRequest = PeriodicWorkRequestBuilder<BillAlertWorker>(
            24, TimeUnit.HOURS
        )
            .addTag("bill_alert")
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "bill_alert_check",
            ExistingPeriodicWorkPolicy.KEEP,
            billAlertRequest
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
    }

    fun cancelBillAlert(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("bill_alert_check")
    }
}