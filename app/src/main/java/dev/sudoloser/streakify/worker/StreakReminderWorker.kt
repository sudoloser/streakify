package dev.sudoloser.streakify.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.sudoloser.streakify.R
import dev.sudoloser.streakify.data.local.dao.StreakDao
import dev.sudoloser.streakify.data.prefs.FilteringMode
import dev.sudoloser.streakify.data.prefs.PreferenceManager
import dev.sudoloser.streakify.util.UsageHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val streakDao: StreakDao,
    private val preferenceManager: PreferenceManager,
    private val usageHelper: UsageHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageHelper.getAppUsageStats(startTime, endTime)
        val threshold = preferenceManager.reminderThreshold.first()
        val filterMode = preferenceManager.filteringMode.first()
        val blacklisted = preferenceManager.blacklistedApps.first()
        val whitelisted = preferenceManager.whitelistedApps.first()

        val streaks = streakDao.getAllStreaks().first()
        
        streaks.forEach { streak ->
            val pkg = streak.packageName
            val isEligible = when (filterMode) {
                FilteringMode.BLACKLIST -> pkg !in blacklisted
                FilteringMode.WHITELIST -> pkg in whitelisted
            }

            if (isEligible && streak.currentStreak >= threshold && (usageStats[pkg] ?: 0L) == 0L) {
                val appName = try {
                    val pm = context.packageManager
                    val info = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    pkg
                }
                showNotification(pkg, appName, streak.currentStreak)
            }
        }

        return Result.success()
    }

    private fun showNotification(pkg: String, appName: String, streak: Int) {
        val channelId = "streak_reminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Streak Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Don't lose your $streak day streak!")
            .setContentText("You haven't used $appName today.")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
            .setAutoCancel(true)
            .build()

        notificationManager.notify(pkg.hashCode(), notification)
    }
}
