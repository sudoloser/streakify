package dev.sudoloser.streakify.util

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getUsageTimeForPackage(packageName: String, startTime: Long, endTime: Long): Long {
        val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        return stats[packageName]?.totalTimeInForeground?.div(1000) ?: 0L
    }

    fun getAppUsageStats(startTime: Long, endTime: Long): Map<String, Long> {
        val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        return stats.mapValues { it.value.totalTimeInForeground / 1000 }
    }
}
