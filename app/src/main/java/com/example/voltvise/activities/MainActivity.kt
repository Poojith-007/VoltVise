package com.example.voltvise.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.voltvise.R
import com.example.voltvise.database.AppDatabase
import com.example.voltvise.utils.BillCalculator
import com.example.voltvise.utils.NotificationHelper
import com.example.voltvise.utils.SmartBillPredictor
import com.example.voltvise.utils.WorkScheduler
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannels(this)
        WorkScheduler.scheduleDailyReminder(this)
        WorkScheduler.scheduleBillAlertCheck(this)

        // Card click handlers (cards replace buttons now)
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.addReadingBtn)
            .setOnClickListener { startActivity(Intent(this, AddReadingActivity::class.java)) }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.historyBtn)
            .setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.chartsBtn)
            .setOnClickListener { startActivity(Intent(this, ChartsActivity::class.java)) }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.tipsBtn)
            .setOnClickListener { startActivity(Intent(this, TipsActivity::class.java)) }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.settingsBtn)
            .setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        findViewById<TextView>(R.id.viewAllCharts)
            .setOnClickListener { startActivity(Intent(this, ChartsActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {
        val todayUsageTv    = findViewById<TextView>(R.id.todayUsage)
        val monthUsageTv    = findViewById<TextView>(R.id.monthUsage)
        val predictedBillTv = findViewById<TextView>(R.id.predictedBill)
        val billPercentTv   = findViewById<TextView>(R.id.billPercent)
        val avgPerDayTv     = findViewById<TextView>(R.id.avgPerDay)
        val avgPerDayBillTv = findViewById<TextView>(R.id.avgPerDayBill)
        val todayTrendTv    = findViewById<TextView>(R.id.todayTrend)
        val billProgress    = findViewById<ProgressBar>(R.id.billProgress)
        val miniChart       = findViewById<BarChart>(R.id.miniChart)
        val greetingText    = findViewById<TextView>(R.id.greetingText)

        // Smart greeting
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        greetingText.text = when {
            hour < 12 -> "Good Morning ☀️"
            hour < 17 -> "Good Afternoon 🌤️"
            else      -> "Good Evening 🌙"
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val readings = db.meterDao().getAllReadings()

            if (readings.isEmpty()) {
                runOnUiThread {
                    todayUsageTv.text  = "0"
                    monthUsageTv.text  = "0"
                    predictedBillTv.text = "₹ 0"
                    avgPerDayTv.text   = "0.0 units/day"
                    avgPerDayBillTv.text = "₹0/day"
                    todayTrendTv.text  = "No data yet"
                }
                return@launch
            }

            val totalUnits   = readings.sumOf { it.unitsUsed }
            val today        = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
            val avgPerDay    = if (today > 0) totalUnits.toFloat() / today else 0f
            val predictedUnits  = SmartBillPredictor.predictMonthlyUnits(totalUnits, today)
            val predictedBillAmt = BillCalculator.calculateBill(predictedUnits)
            val daysInMonth  = java.util.Calendar.getInstance()
                .getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val monthProgress   = ((today.toFloat() / daysInMonth) * 100).toInt()
            val todayUnits      = readings.first().unitsUsed
            val yesterdayUnits  = if (readings.size > 1) readings[1].unitsUsed else -1
            val dailyBillRate   = if (today > 0) predictedBillAmt / today else 0

            val trendText = when {
                yesterdayUnits < 0   -> "First reading"
                todayUnits > yesterdayUnits ->
                    "↑ ${todayUnits - yesterdayUnits} more than prev"
                todayUnits < yesterdayUnits ->
                    "↓ ${yesterdayUnits - todayUnits} less than prev"
                else -> "→ Same as previous"
            }

            val last7 = readings.take(7).reversed()

            runOnUiThread {
                todayUsageTv.text    = "$todayUnits"
                monthUsageTv.text    = "$totalUnits"
                predictedBillTv.text = "₹ $predictedBillAmt"
                billPercentTv.text   = "$monthProgress% of month"
                avgPerDayTv.text     = "${"%.1f".format(avgPerDay)} units/day"
                avgPerDayBillTv.text = "₹$dailyBillRate/day"
                todayTrendTv.text    = trendText
                billProgress.progress = monthProgress
                setupMiniChart(miniChart, last7)
            }
        }
    }

    private fun setupMiniChart(
        chart: BarChart,
        readings: List<com.example.voltvise.database.MeterEntity>
    ) {
        val entries = readings.mapIndexed { i, r ->
            BarEntry(i.toFloat(), r.unitsUsed.toFloat())
        }

        val labels = readings.map { it.date }

        val dataSet = BarDataSet(entries, "Units").apply {
            color = 0xFF1565C0.toInt()
            valueTextColor = 0xFF333333.toInt()
            valueTextSize = 9f
        }

        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            axisLeft.apply {
                textSize = 10f
                textColor = 0xFF555555.toInt()
                setDrawGridLines(true)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 9f
                textColor = 0xFF555555.toInt()
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
            }
            animateY(600)
            invalidate()
        }
    }
}