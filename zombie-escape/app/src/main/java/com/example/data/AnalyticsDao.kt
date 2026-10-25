package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AnalyticsRecord): Long

    @Query("SELECT * FROM analytics_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalyticsRecord>>

    @Query("SELECT * FROM analytics_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int = 20): List<AnalyticsRecord>

    @Query("SELECT COUNT(*) FROM analytics_records WHERE escaped = 1")
    suspend fun getEscapeCount(): Int

    @Query("SELECT COUNT(*) FROM analytics_records WHERE escaped = 0")
    suspend fun getDeathCount(): Int

    @Query("SELECT AVG(timeElapsedSec) FROM analytics_records WHERE levelNumber = :level AND escaped = 1")
    suspend fun getAvgEscapeTime(level: Int): Double?
}
