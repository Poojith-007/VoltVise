package com.example.voltvise.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object AlarmScheduler {

    fun scheduleDailyReminder(context: Context) {
        val prefs = context.getSharedPreferences(
            "voltvise_prefs", Context.MODE_PRIVATE
        )
        val enabled = prefs.getBoolean("reminder_enabled", true)
        if (!enabled) {
            cancelReminder(context)
            return
        }

        val hour   = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)

        // Build the target calendar time
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today → set for tomorrow
        if (target.timeInMillis <= System.currentTimeMillis()) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager

        val pendingIntent = buildPendingIntent(context)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ — check if exact alarms are allowed
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "Exact alarm set for ${target.time}")
                } else {
                    // Fallback to inexact if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "Inexact alarm set for ${target.time}")
                }
            } else {
                // Below Android 12 — exact alarm always works
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
                Log.d("AlarmScheduler", "Exact alarm set for ${target.time}")
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to set alarm: ${e.message}")
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager
        alarmManager.cancel(buildPendingIntent(context))
        Log.d("AlarmScheduler", "Reminder cancelled")
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DAILY_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}