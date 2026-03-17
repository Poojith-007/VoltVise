package com.example.voltvise.activities

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voltvise.R
import com.example.voltvise.utils.WorkScheduler
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.work.WorkManager

class SettingsActivity : AppCompatActivity() {

    private var selectedHour   = 20
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = getSharedPreferences("voltvise_prefs", Context.MODE_PRIVATE)

        val reminderSwitch   = findViewById<SwitchMaterial>(R.id.reminderSwitch)
        val billAlertSwitch  = findViewById<SwitchMaterial>(R.id.billAlertSwitch)
        val reminderTimeText = findViewById<TextView>(R.id.reminderTimeText)
        val thresholdValue   = findViewById<TextView>(R.id.thresholdValue)
        val thresholdSeekBar = findViewById<SeekBar>(R.id.thresholdSeekBar)
        val saveBtn          = findViewById<MaterialButton>(R.id.saveSettingsBtn)

        // Load saved preferences
        reminderSwitch.isChecked  = prefs.getBoolean("reminder_enabled", true)
        billAlertSwitch.isChecked = prefs.getBoolean("bill_alert_enabled", true)
        selectedHour   = prefs.getInt("reminder_hour", 20)
        selectedMinute = prefs.getInt("reminder_minute", 0)
        val savedThreshold = prefs.getInt("bill_alert_threshold", 1500)
        thresholdSeekBar.progress = savedThreshold
        thresholdValue.text = "₹$savedThreshold"
        reminderTimeText.text = formatTime(selectedHour, selectedMinute)

        // Time picker on tap
        reminderTimeText.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedHour   = hour
                selectedMinute = minute
                reminderTimeText.text = formatTime(hour, minute)
            }, selectedHour, selectedMinute, false).show()
        }

        // Threshold seekbar
        thresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Snap to nearest 100
                val snapped = (progress / 100) * 100
                thresholdValue.text = "₹$snapped"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save button
        saveBtn.setOnClickListener {
            val threshold = (thresholdSeekBar.progress / 100) * 100

            prefs.edit().apply {
                putBoolean("reminder_enabled", reminderSwitch.isChecked)
                putBoolean("bill_alert_enabled", billAlertSwitch.isChecked)
                putInt("reminder_hour", selectedHour)
                putInt("reminder_minute", selectedMinute)
                putInt("bill_alert_threshold", threshold)
                apply()
            }

            // Schedule or cancel based on toggle
            if (reminderSwitch.isChecked) {
                WorkScheduler.scheduleDailyReminder(this, selectedHour, selectedMinute)
            } else {
                WorkScheduler.cancelReminder(this)
            }

            if (billAlertSwitch.isChecked) {
                WorkScheduler.scheduleBillAlertCheck(this)
            } else {
                WorkManager.getInstance(this).cancelUniqueWork("bill_alert_check")
            }

            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val h    = if (hour % 12 == 0) 12 else hour % 12
        val m    = minute.toString().padStart(2, '0')
        return "$h:$m $amPm"
    }
}