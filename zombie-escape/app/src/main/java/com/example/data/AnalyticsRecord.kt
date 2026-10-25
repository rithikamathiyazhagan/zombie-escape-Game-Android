package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Feature 14: Analytics
 * Tracks player behavioral metrics across sessions to help evaluate and improve difficulty balance.
 */
@Entity(tableName = "analytics_records")
data class AnalyticsRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val levelNumber: Int,
    val escaped: Boolean,
    val timeElapsedSec: Int,
    val stepsTaken: Int,
    val zombieAlertTriggers: Int,
    val damageTaken: Int,
    val sprintSeconds: Float,
    val decoysUsed: Int,
    val difficultyRating: String // "Fair & Balanced", "High Lethality", "Speedrunner", "Sneak Specialist"
)
