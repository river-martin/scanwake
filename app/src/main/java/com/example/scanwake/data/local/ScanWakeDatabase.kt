package com.example.scanwake.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.example.scanwake.data.model.Alarm
import com.example.scanwake.data.model.SavedBarcode

@Database(entities = [Alarm::class, SavedBarcode::class], version = 3, exportSchema = false)
abstract class ScanWakeDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun savedBarcodeDao(): SavedBarcodeDao

    companion object {
        @Volatile
        private var INSTANCE: ScanWakeDatabase? = null

        fun getDatabase(context: Context): ScanWakeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScanWakeDatabase::class.java,
                    "scanwake_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
