package com.example.voltvise.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeterDao {

    @Insert
    suspend fun insertReading(reading: MeterEntity)

    @Query("SELECT * FROM meter_readings ORDER BY id DESC")
    suspend fun getAllReadings(): List<MeterEntity>

    @Query("SELECT * FROM meter_readings ORDER BY id DESC LIMIT 1")
    suspend fun getLastReading(): MeterEntity?
}