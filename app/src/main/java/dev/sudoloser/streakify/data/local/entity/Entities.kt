package dev.sudoloser.streakify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class Streak(
    @PrimaryKey val packageName: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastUpdatedDate: Long = 0L // Timestamp of last usage recorded
)

@Entity(tableName = "usage_records")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val date: Long, // Start of day timestamp
    val usageSeconds: Long
)
