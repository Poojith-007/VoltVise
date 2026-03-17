package com.example.voltvise.models

data class MeterReading(
    val date: String,
    val reading: Int,
    val unitsUsed: Int
)