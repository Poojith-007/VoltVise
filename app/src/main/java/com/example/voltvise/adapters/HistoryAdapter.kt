package com.example.voltvise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voltvise.R
import com.example.voltvise.database.MeterEntity
import com.example.voltvise.models.MeterReading

class HistoryAdapter(private val list: List<MeterEntity>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val dateText: TextView = view.findViewById(R.id.dateText)
        val readingText: TextView = view.findViewById(R.id.readingText)
        val unitText: TextView = view.findViewById(R.id.unitText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.dateText.text = item.date
        holder.readingText.text = "Reading: ${item.reading}"
        holder.unitText.text = "Units: ${item.unitsUsed}"
    }

    override fun getItemCount(): Int {
        return list.size
    }
}