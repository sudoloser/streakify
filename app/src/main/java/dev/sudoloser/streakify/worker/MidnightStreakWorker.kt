package dev.sudoloser.streakify.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.sudoloser.streakify.data.local.dao.StreakDao
import dev.sudoloser.streakify.data.local.entity.Streak
import dev.sudoloser.streakify.data.prefs.FilteringMode
import dev.sudoloser.streakify.data.prefs.PreferenceManager
import dev.sudoloser.streakify.util.UsageHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*

@HiltWorker
class MidnightStreakWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val streakDao: StreakDao,
    private val preferenceManager: PreferenceManager,
    private val usageHelper: UsageHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        val usageStats = usageHelper.getAppUsageStats(startTime, endTime)
        val filterMode = preferenceManager.filteringMode.first()
        val blacklisted = preferenceManager.blacklistedApps.first()
        val whitelisted = preferenceManager.whitelistedApps.first()

        // Get all known streaks
        val allStreaks = streakDao.getAllStreaks().first()
        val trackedPackages = mutableSetOf<String>()

        when (filterMode) {
            FilteringMode.BLACKLIST -> {
                // In blacklist mode, we track everything not in the blacklist
                // We need a list of all installed apps that were used
                usageStats.keys.forEach { pkg ->
                    if (pkg !in blacklisted) trackedPackages.add(pkg)
                }
                // Also check existing streaks
                allStreaks.forEach { if (it.packageName !in blacklisted) trackedPackages.add(it.packageName) }
            }
            FilteringMode.WHITELIST -> {
                trackedPackages.addAll(whitelisted)
            }
        }

        trackedPackages.forEach { pkg ->
            val usage = usageStats[pkg] ?: 0L
            val existingStreak = streakDao.getStreak(pkg) ?: Streak(pkg)
            
            val newStreak = if (usage > 0) {
                val current = existingStreak.currentStreak + 1
                existingStreak.copy(
                    currentStreak = current,
                    longestStreak = maxOf(current, existingStreak.longestStreak),
                    lastUpdatedDate = System.currentTimeMillis()
                )
            } else {
                existingStreak.copy(currentStreak = 0)
            }
            streakDao.upsertStreak(newStreak)
        }

        return Result.success()
    }
}
