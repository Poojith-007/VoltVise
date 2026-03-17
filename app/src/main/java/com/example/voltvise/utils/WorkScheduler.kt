package com.example.voltvise.utils

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkScheduler {

    // Schedule daily reminder at user's chosen time (default 8:00 PM)
    fun scheduleDailyReminder(context: Context, hourOfDay: Int = 20, minute: Int = 0) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If chosen time already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "daily_reminder",
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    // Schedule bill alert check — runs every day
    fun scheduleBillAlertCheck(context: Context) {
        val billAlertRequest = PeriodicWorkRequestBuilder<BillAlertWorker>(
            1, TimeUnit.DAYS
        )
            .addTag("bill_alert")
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
}