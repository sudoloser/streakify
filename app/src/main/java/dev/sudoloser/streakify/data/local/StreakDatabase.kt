package dev.sudoloser.streakify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.sudoloser.streakify.data.local.dao.StreakDao
import dev.sudoloser.streakify.data.local.entity.AppUsageRecord
import dev.sudoloser.streakify.data.local.entity.Streak

@Database(entities = [Streak::class, AppUsageRecord::class], version = 1, exportSchema = false)
abstract class StreakDatabase : RoomDatabase() {
    abstract fun streakDao(): StreakDao
}
