package com.example.voltvise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeterEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun meterDao(): MeterDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            if (INSTANCE == null) {

                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electricity_db"
                ).build()
            }

            return INSTANCE!!
        }
    }
}