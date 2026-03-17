package com.example.voltvise.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.voltvise.R

class TipsActivity : AppCompatActivity() {

    // Data class for each tip
    data class Tip(
        val icon: String,
        val title: String,
        val description: String,
        val saving: String
    )

    // All tips grouped by category
    private val lightingTips = listOf(
        Tip("💡", "Switch to LED bulbs",
            "LED bulbs use 75% less energy than incandescent bulbs and last 25x longer.",
            "↓15%"),
        Tip("🌞", "Use natural light",
            "Open curtains during the day instead of switching on lights.",
            "↓5%"),
        Tip("⏱️", "Use timers for lights",
            "Install automatic timers or motion sensors so lights turn off when not needed.",
            "↓8%")
    )

    private val applianceTips = listOf(
        Tip("🌡️", "Use inverter appliances",
            "Inverter ACs, fridges and washing machines adjust power usage automatically.",
            "↓20%"),
        Tip("🚿", "Use cold water for laundry",
            "Heating water accounts for 90% of washing machine energy. Use cold cycles.",
            "↓10%"),
        Tip("🍳", "Match pot size to burner",
            "Using a small pan on a large electric burner wastes significant energy.",
            "↓3%"),
        Tip("🧊", "Keep fridge coils clean",
            "Dusty condenser coils make your fridge work harder. Clean them every 6 months.",
            "↓6%")
    )

    private val coolingTips = listOf(
        Tip("❄️", "Set AC to 24°C or higher",
            "Each degree below 24°C increases energy consumption by about 6%.",
            "↓18%"),
        Tip("🪟", "Seal windows and doors",
            "Air leaks force your AC to work harder. Use weather stripping to seal gaps.",
            "↓12%"),
        Tip("🌀", "Use ceiling fans first",
            "Ceiling fans use 60W vs 1500W for AC. Use fans before switching on the AC.",
            "↓25%")
    )

    private val standbyTips = listOf(
        Tip("🔌", "Unplug idle chargers",
            "Phone chargers and adapters consume power even when nothing is connected.",
            "↓4%"),
        Tip("📺", "Turn off standby mode",
            "TVs, set-top boxes and microwaves on standby can waste up to 10% of your bill.",
            "↓10%")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        setSupportActionBar(findViewById(
            com.google.android.material.R.id.action_bar
        ))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Bind lighting tips
        bindTip(R.id.tip1, lightingTips[0])
        bindTip(R.id.tip2, lightingTips[1])
        bindTip(R.id.tip3, lightingTips[2])

        // Bind appliance tips
        bindTip(R.id.tip4, applianceTips[0])
        bindTip(R.id.tip5, applianceTips[1])
        bindTip(R.id.tip6, applianceTips[2])
        bindTip(R.id.tip7, applianceTips[3])

        // Bind cooling tips
        bindTip(R.id.tip8, coolingTips[0])
        bindTip(R.id.tip9, coolingTips[1])
        bindTip(R.id.tip10, coolingTips[2])

        // Bind standby tips
        bindTip(R.id.tip11, standbyTips[0])
        bindTip(R.id.tip12, standbyTips[1])
    }

    private fun bindTip(viewId: Int, tip: Tip) {
        val view = findViewById<android.view.View>(viewId)
        view.findViewById<TextView>(R.id.tipIcon).text    = tip.icon
        view.findViewById<TextView>(R.id.tipTitle).text   = tip.title
        view.findViewById<TextView>(R.id.tipDesc).text    = tip.description
        view.findViewById<TextView>(R.id.tipSaving).text  = tip.saving
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}