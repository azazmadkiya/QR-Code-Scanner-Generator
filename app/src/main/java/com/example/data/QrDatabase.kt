package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QrEntity::class], version = 1, exportSchema = false)
abstract class QrDatabase : RoomDatabase() {
    abstract fun qrDao(): QrDao

    companion object {
        @Volatile
        private var INSTANCE: QrDatabase? = null

        fun getDatabase(context: Context): QrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QrDatabase::class.java,
                    "qr_history_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
