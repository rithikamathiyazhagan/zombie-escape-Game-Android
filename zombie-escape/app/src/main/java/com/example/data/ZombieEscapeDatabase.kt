package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AnalyticsRecord::class],
    version = 1,
    exportSchema = false
)
abstract class ZombieEscapeDatabase : RoomDatabase() {
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: ZombieEscapeDatabase? = null

        fun getDatabase(context: Context): ZombieEscapeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZombieEscapeDatabase::class.java,
                    "zombie_escape_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
