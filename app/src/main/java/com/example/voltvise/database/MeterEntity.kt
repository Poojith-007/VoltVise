package com.example.voltvise.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meter_readings")
data class MeterEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,
    val reading: Int,
    val unitsUsed: Int
)