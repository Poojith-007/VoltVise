package com.example.voltvise.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voltvise.R
import com.example.voltvise.adapters.HistoryAdapter
import com.example.voltvise.database.AppDatabase
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

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

        val recyclerView = findViewById<RecyclerView>(R.id.historyRecycler)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Attach empty adapter first
        val adapter = HistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {

            val readings = db.meterDao().getAllReadings()

            runOnUiThread {

                recyclerView.adapter = HistoryAdapter(readings)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}