package com.example.voltvise.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.voltvise.R

class TipsFragment : Fragment() {

    data class Tip(val icon: String, val title: String,
                   val description: String, val saving: String)

    private val lightingTips = listOf(
        Tip("💡", "Switch to LED bulbs",
            "LED bulbs use 75% less energy than incandescent bulbs and last 25x longer.", "↓15%"),
        Tip("🌞", "Use natural light",
            "Open curtains during the day instead of switching on lights.", "↓5%"),
        Tip("⏱️", "Use timers for lights",
            "Install automatic timers so lights turn off when not needed.", "↓8%")
    )
    private val applianceTips = listOf(
        Tip("🌡️", "Use inverter appliances",
            "Inverter ACs and fridges adjust power usage automatically.", "↓20%"),
        Tip("🚿", "Use cold water for laundry",
            "Heating water accounts for 90% of washing machine energy.", "↓10%"),
        Tip("🍳", "Match pot size to burner",
            "Using a small pan on a large electric burner wastes energy.", "↓3%"),
        Tip("🧊", "Keep fridge coils clean",
            "Dusty condenser coils make your fridge work harder.", "↓6%")
    )
    private val coolingTips = listOf(
        Tip("❄️", "Set AC to 24°C or higher",
            "Each degree below 24°C increases energy consumption by about 6%.", "↓18%"),
        Tip("🪟", "Seal windows and doors",
            "Air leaks force your AC to work harder. Use weather stripping.", "↓12%"),
        Tip("🌀", "Use ceiling fans first",
            "Ceiling fans use 60W vs 1500W for AC.", "↓25%")
    )
    private val standbyTips = listOf(
        Tip("🔌", "Unplug idle chargers",
            "Phone chargers consume power even when nothing is connected.", "↓4%"),
        Tip("📺", "Turn off standby mode",
            "TVs and set-top boxes on standby waste up to 10% of your bill.", "↓10%")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tips, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindTip(view, R.id.tip1, lightingTips[0])
        bindTip(view, R.id.tip2, lightingTips[1])
        bindTip(view, R.id.tip3, lightingTips[2])
        bindTip(view, R.id.tip4, applianceTips[0])
        bindTip(view, R.id.tip5, applianceTips[1])
        bindTip(view, R.id.tip6, applianceTips[2])
        bindTip(view, R.id.tip7, applianceTips[3])
        bindTip(view, R.id.tip8, coolingTips[0])
        bindTip(view, R.id.tip9, coolingTips[1])
        bindTip(view, R.id.tip10, coolingTips[2])
        bindTip(view, R.id.tip11, standbyTips[0])
        bindTip(view, R.id.tip12, standbyTips[1])
    }

    private fun bindTip(root: View, viewId: Int, tip: Tip) {
        val v = root.findViewById<View>(viewId)
        v.findViewById<TextView>(R.id.tipIcon).text   = tip.icon
        v.findViewById<TextView>(R.id.tipTitle).text  = tip.title
        v.findViewById<TextView>(R.id.tipDesc).text   = tip.description
        v.findViewById<TextView>(R.id.tipSaving).text = tip.saving
    }
}