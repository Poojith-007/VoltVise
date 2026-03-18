package com.example.voltvise.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.voltvise.R
import com.example.voltvise.database.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class ChartsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        // Safe version — won't crash if toolbar not found
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        val lineChart = findViewById<LineChart>(R.id.usageLineChart)
        val barChart  = findViewById<BarChart>(R.id.usageBarChart)

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val readings = db.meterDao().getAllReadings().reversed()
            val labels   = readings.map { it.date }

            val lineEntries = readings.mapIndexed { i, r ->
                Entry(i.toFloat(), r.unitsUsed.toFloat())
            }
            val barEntries = readings.mapIndexed { i, r ->
                BarEntry(i.toFloat(), r.unitsUsed.toFloat())
            }

            runOnUiThread {
                // ── Line Chart ──────────────────────────────
                val lineSet = LineDataSet(lineEntries, "Units Used").apply {
                    color = 0xFF1565C0.toInt()
                    setCircleColor(0xFF1565C0.toInt())
                    lineWidth = 2.5f
                    circleRadius = 4f
                    setDrawFilled(true)
                    fillColor = 0xFF90CAF9.toInt()
                    fillAlpha = 80
                    valueTextSize = 10f
                    valueTextColor = 0xFF333333.toInt()
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                lineChart.apply {
                    data = LineData(lineSet)
                    description.text = "Usage Trend"
                    description.textSize = 12f
                    setDrawGridBackground(false)
                    axisRight.isEnabled = false
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(labels)
                        granularity = 1f
                        setDrawGridLines(false)
                        textColor = 0xFF555555.toInt()
                    }
                    axisLeft.textColor = 0xFF555555.toInt()
                    animateXY(800, 800)
                    invalidate()
                }

                // ── Bar Chart ───────────────────────────────
                val barSet = BarDataSet(barEntries, "Units Per Reading").apply {
                    color = 0xFF00897B.toInt()
                    valueTextSize = 10f
                    valueTextColor = 0xFF333333.toInt()
                }

                barChart.apply {
                    data = BarData(barSet).apply { barWidth = 0.6f }
                    description.text = "Units Per Entry"
                    description.textSize = 12f
                    setDrawGridBackground(false)
                    axisRight.isEnabled = false
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(labels)
                        granularity = 1f
                        setDrawGridLines(false)
                        textColor = 0xFF555555.toInt()
                    }
                    axisLeft.textColor = 0xFF555555.toInt()
                    animateY(800)
                    invalidate()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}