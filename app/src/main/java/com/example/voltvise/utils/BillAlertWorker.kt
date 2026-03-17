package com.example.voltvise.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.voltvise.database.AppDatabase

class BillAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(context)
        val readings = db.meterDao().getAllReadings()

        val totalUnits = readings.sumOf { it.unitsUsed }
        val today = java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_MONTH)

        val predictedUnits = SmartBillPredictor.predictMonthlyUnits(totalUnits, today)
        val predictedBill  = BillCalculator.calculateBill(predictedUnits)

        // Load threshold from SharedPreferences (default ₹1500)
        val prefs = context.getSharedPreferences("voltvise_prefs", Context.MODE_PRIVATE)
        val threshold = prefs.getInt("bill_alert_threshold", 1500)

        NotificationHelper.showBillAlert(context, predictedBill, threshold)

        return Result.success()
    }
}