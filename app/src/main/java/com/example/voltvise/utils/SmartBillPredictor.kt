package com.example.voltvise.utils

object SmartBillPredictor {

    fun predictMonthlyUnits(totalUnits: Int, daysPassed: Int): Int {

        if (daysPassed == 0) return 0

        val averagePerDay = totalUnits.toFloat() / daysPassed

        val predictedUnits = averagePerDay * 30

        return predictedUnits.toInt()
    }

}