package com.example.voltvise.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.voltvise.R
import com.example.voltvise.utils.NotificationHelper
import com.example.voltvise.utils.WorkScheduler
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.voltvise.utils.AlarmScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    // Permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AlarmScheduler.scheduleDailyReminder(this)  // ✅ Changed
            WorkScheduler.scheduleBillAlertCheck(this)
        } else {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Notifications Disabled")
                .setMessage("You won't receive reminders. Enable in phone Settings → Apps → VoltVise → Notifications.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannels(this)

        // ✅ Ask notification permission properly
        requestNotificationPermission()

        // Add after requestNotificationPermission()
        requestBatteryOptimizationExemption()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            bottomNav.selectedItemId = R.id.nav_dashboard
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(DashboardFragment())
                R.id.nav_charts    -> loadFragment(ChartsFragment())
                R.id.nav_tips      -> loadFragment(TipsFragment())
                R.id.nav_profile   -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // ✅ Use AlarmScheduler now
                    AlarmScheduler.scheduleDailyReminder(this)
                    WorkScheduler.scheduleBillAlertCheck(this)
                }
                else -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } else {
            // ✅ Use AlarmScheduler now
            AlarmScheduler.scheduleDailyReminder(this)
            WorkScheduler.scheduleBillAlertCheck(this)
        }
    }
    // Ask user to disable battery optimization for reliable reminders
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(android.content.Context.POWER_SERVICE)
                    as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Enable Reliable Reminders")
                    .setMessage("To receive reminders on time, please disable battery optimization for VoltVise.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            android.net.Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton("Skip", null)
                    .show()
            }
        }
    }

    fun navigateTo(navId: Int) {
        bottomNav.selectedItemId = navId
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(android.content.Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_notifications -> {
                startActivity(android.content.Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("⚡ VoltVise")
                    .setMessage("Smart Electricity Tracker\nVersion 1.0")
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}