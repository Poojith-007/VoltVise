package com.example.voltvise.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.voltvise.database.AppDatabase

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(context)
        val readings = db.meterDao().getAllReadings()

        // Only remind if user hasn't added a reading today
        val todayDate = java.text.SimpleDateFormat(
            "dd MMM", java.util.Locale.getDefault()
        ).format(java.util.Date())

        val alreadyEnteredToday = readings.isNotEmpty() &&
                readings.first().date == todayDate

        if (!alreadyEnteredToday) {
            NotificationHelper.showDailyReminder(context)
        }

        return Result.success()
    }
}