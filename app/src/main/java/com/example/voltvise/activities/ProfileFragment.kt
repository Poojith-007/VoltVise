package com.example.voltvise.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.voltvise.R
import com.example.voltvise.database.AppDatabase
import com.example.voltvise.utils.BillCalculator
import com.example.voltvise.utils.SmartBillPredictor
import kotlinx.coroutines.launch
import java.util.Calendar

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.goToSettings).setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.clearData).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("This will delete all your meter readings permanently. Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        AppDatabase.getDatabase(requireContext()).meterDao().deleteAll()
                        loadStats(view)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadStats(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadStats(it) }
    }

    private fun loadStats(view: View) {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val readings        = db.meterDao().getAllReadings()
            val totalUnits      = readings.sumOf { it.unitsUsed }
            val today           = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val predictedUnits  = SmartBillPredictor.predictMonthlyUnits(totalUnits, today)
            val predictedBill   = BillCalculator.calculateBill(predictedUnits)

            activity?.runOnUiThread {
                view.findViewById<TextView>(R.id.statReadings).text = "${readings.size}"
                view.findViewById<TextView>(R.id.statUnits).text    = "$totalUnits"
                view.findViewById<TextView>(R.id.statBill).text     = "₹$predictedBill"
            }
        }
    }
}