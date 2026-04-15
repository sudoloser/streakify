package dev.sudoloser.streakify.ui.dashboard

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sudoloser.streakify.data.local.dao.StreakDao
import dev.sudoloser.streakify.data.local.entity.Streak
import dev.sudoloser.streakify.data.prefs.FilteringMode
import dev.sudoloser.streakify.data.prefs.PreferenceManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AppStreakInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val currentStreak: Int,
    val longestStreak: Int
)

data class DashboardState(
    val streaks: List<AppStreakInfo> = emptyList(),
    val isLoading: Boolean = true,
    val filteringMode: FilteringMode = FilteringMode.BLACKLIST
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val streakDao: StreakDao,
    private val preferenceManager: PreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val pm = context.packageManager

    val state: StateFlow<DashboardState> = combine(
        streakDao.getAllStreaks(),
        preferenceManager.filteringMode,
        preferenceManager.blacklistedApps,
        preferenceManager.whitelistedApps
    ) { streaks, mode, blacklisted, whitelisted ->
        val filteredStreaks = streaks.filter { streak ->
            when (mode) {
                FilteringMode.BLACKLIST -> streak.packageName !in blacklisted
                FilteringMode.WHITELIST -> streak.packageName in whitelisted
            }
        }.map { streak ->
            val appInfo = try {
                val info = pm.getApplicationInfo(streak.packageName, 0)
                AppStreakInfo(
                    packageName = streak.packageName,
                    appName = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info),
                    currentStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak
                )
            } catch (e: Exception) {
                AppStreakInfo(
                    packageName = streak.packageName,
                    appName = streak.packageName,
                    icon = null,
                    currentStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak
                )
            }
            appInfo
        }.sortedByDescending { it.currentStreak }

        DashboardState(
            streaks = filteredStreaks,
            isLoading = false,
            filteringMode = mode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
}
