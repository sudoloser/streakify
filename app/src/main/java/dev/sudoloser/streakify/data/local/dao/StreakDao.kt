package dev.sudoloser.streakify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.sudoloser.streakify.data.local.entity.Streak
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<Streak>>

    @Query("SELECT * FROM streaks WHERE packageName = :packageName")
    suspend fun getStreak(packageName: String): Streak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: Streak)

    @Update
    suspend fun updateStreak(streak: Streak)

    @Query("DELETE FROM streaks WHERE packageName = :packageName")
    suspend fun deleteStreak(packageName: String)
}
