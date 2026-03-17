package com.example.voltvise.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.voltvise.database.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DAILY_REMINDER -> handleDailyReminder(context)
            ACTION_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED -> rescheduleAfterBoot(context)
        }
    }

    private fun handleDailyReminder(context: Context) {
        // Check if user already entered reading today
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val readings = db.meterDao().getAllReadings()

            val todayDate = SimpleDateFormat(
                "dd MMM", Locale.getDefault()
            ).format(Date())

            val alreadyEnteredToday = readings.isNotEmpty() &&
                    readings.first().date == todayDate

            if (!alreadyEnteredToday) {
                NotificationHelper.showDailyReminder(context)
            }

            // Reschedule for next day at same time
            AlarmScheduler.scheduleDailyReminder(context)
        }
    }

    private fun rescheduleAfterBoot(context: Context) {
        // Alarms are cleared on reboot — reschedule them
        val prefs = context.getSharedPreferences(
            "voltvise_prefs", Context.MODE_PRIVATE
        )
        val enabled = prefs.getBoolean("reminder_enabled", true)
        if (enabled) {
            AlarmScheduler.scheduleDailyReminder(context)
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.voltvise.DAILY_REMINDER"
        const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    }
}