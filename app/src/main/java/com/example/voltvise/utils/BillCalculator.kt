package com.example.voltvise.utils

object BillCalculator {

    fun calculateBill(units: Int): Int {

        var bill = 0

        if (units <= 100) {

            bill = units * 3

        } else if (units <= 200) {

            bill = (100 * 3) + ((units - 100) * 5)

        } else {

            bill = (100 * 3) + (100 * 5) + ((units - 200) * 7)
        }

        return bill
    }
}