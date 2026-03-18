package com.example.voltvise.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.voltvise.R
import com.example.voltvise.database.AppDatabase
import com.example.voltvise.database.MeterEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddReadingActivity : AppCompatActivity() {

    private lateinit var readingInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reading)

        // Safe version — won't crash if toolbar not found
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        readingInput = findViewById(R.id.meterReadingInput)

        val saveBtn = findViewById<Button>(R.id.saveReadingBtn)
        val scanBtn = findViewById<Button>(R.id.scanMeterBtn)

        val db = AppDatabase.getDatabase(this)

        saveBtn.setOnClickListener {

            val readingText = readingInput.text.toString()

            if (readingText.isEmpty()) {

                Toast.makeText(this, "Enter meter reading", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reading = readingText.toInt()

            lifecycleScope.launch {

                val lastReading = db.meterDao().getLastReading()

                val units = if (lastReading == null) {
                    0
                } else {
                    reading - lastReading.reading
                }

                val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

                val entity = MeterEntity(
                    date = date,
                    reading = reading,
                    unitsUsed = units
                )

                db.meterDao().insertReading(entity)

                runOnUiThread {

                    Toast.makeText(
                        this@AddReadingActivity,
                        "Reading Saved",
                        Toast.LENGTH_SHORT
                    ).show()

                    readingInput.text.clear()
                }
            }
        }

        scanBtn.setOnClickListener {

            val intent = Intent(this, MeterScannerActivity::class.java)

            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {

            val meterValue = data?.getStringExtra("meter_value")

            readingInput.setText(meterValue)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}