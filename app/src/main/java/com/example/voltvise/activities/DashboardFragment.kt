package com.example.voltvise.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.voltvise.R
import com.example.voltvise.database.AppDatabase
import com.example.voltvise.utils.BillCalculator
import com.example.voltvise.utils.SmartBillPredictor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialCardView>(R.id.addReadingBtn).setOnClickListener {
            startActivity(Intent(requireContext(), AddReadingActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.historyQuickBtn).setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        view.findViewById<TextView>(R.id.viewAllCharts).setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_charts)
        }

        loadDashboard(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadDashboard(it) }
    }

    private fun loadDashboard(view: View) {
        val todayUsageTv    = view.findViewById<TextView>(R.id.todayUsage)
        val monthUsageTv    = view.findViewById<TextView>(R.id.monthUsage)
        val predictedBillTv = view.findViewById<TextView>(R.id.predictedBill)
        val billPercentTv   = view.findViewById<TextView>(R.id.billPercent)
        val avgPerDayTv     = view.findViewById<TextView>(R.id.avgPerDay)
        val avgPerDayBillTv = view.findViewById<TextView>(R.id.avgPerDayBill)
        val todayTrendTv    = view.findViewById<TextView>(R.id.todayTrend)
        val billProgress    = view.findViewById<ProgressBar>(R.id.billProgress)
        val miniChart       = view.findViewById<BarChart>(R.id.miniChart)
        val greetingText    = view.findViewById<TextView>(R.id.greetingText)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        greetingText.text = when {
            hour < 12 -> "Good Morning ☀️"
            hour < 17 -> "Good Afternoon 🌤️"
            else      -> "Good Evening 🌙"
        }

        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val readings = db.meterDao().getAllReadings()
            if (readings.isEmpty()) {
                activity?.runOnUiThread {
                    todayUsageTv.text    = "0"
                    monthUsageTv.text    = "0"
                    predictedBillTv.text = "₹ 0"
                    avgPerDayTv.text     = "0.0 units/day"
                    avgPerDayBillTv.text = "₹0/day"
                    todayTrendTv.text    = "No data yet"
                }
                return@launch
            }

            val totalUnits      = readings.sumOf { it.unitsUsed }
            val today           = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val avgPerDay       = if (today > 0) totalUnits.toFloat() / today else 0f
            val predictedUnits  = SmartBillPredictor.predictMonthlyUnits(totalUnits, today)
            val predictedBillAmt = BillCalculator.calculateBill(predictedUnits)
            val daysInMonth     = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthProgress   = ((today.toFloat() / daysInMonth) * 100).toInt()
            val todayUnits      = readings.first().unitsUsed
            val yesterdayUnits  = if (readings.size > 1) readings[1].unitsUsed else -1
            val dailyBillRate   = if (today > 0) predictedBillAmt / today else 0

            val trendText = when {
                yesterdayUnits < 0              -> "First reading"
                todayUnits > yesterdayUnits     -> "↑ ${todayUnits - yesterdayUnits} more than prev"
                todayUnits < yesterdayUnits     -> "↓ ${yesterdayUnits - todayUnits} less than prev"
                else                            -> "→ Same as previous"
            }

            val last7 = readings.take(7).reversed()

            activity?.runOnUiThread {
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
        val entries = readings.mapIndexed { i, r -> BarEntry(i.toFloat(), r.unitsUsed.toFloat()) }
        val labels  = readings.map { it.date }
        val dataSet = BarDataSet(entries, "Units").apply {
            color          = 0xFF1565C0.toInt()
            valueTextColor = 0xFF333333.toInt()
            valueTextSize  = 9f
        }
        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled      = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            axisLeft.apply { textSize = 10f; textColor = 0xFF555555.toInt() }
            xAxis.apply {
                position      = XAxis.XAxisPosition.BOTTOM
                textSize      = 9f
                textColor     = 0xFF555555.toInt()
                setDrawGridLines(false)
                granularity   = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
            }
            animateY(600)
            invalidate()
        }
    }
}