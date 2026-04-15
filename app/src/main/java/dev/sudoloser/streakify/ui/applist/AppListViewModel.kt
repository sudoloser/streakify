package dev.sudoloser.streakify.ui.applist

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sudoloser.streakify.data.prefs.FilteringMode
import dev.sudoloser.streakify.data.prefs.PreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSelected: Boolean
)

data class AppListState(
    val apps: List<AppInfo> = emptyList(),
    val filteringMode: FilteringMode = FilteringMode.BLACKLIST,
    val searchQuery: String = ""
)

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val pm = context.packageManager
    private val _searchQuery = MutableStateFlow("")

    val state: StateFlow<AppListState> = combine(
        preferenceManager.filteringMode,
        preferenceManager.blacklistedApps,
        preferenceManager.whitelistedApps,
        _searchQuery
    ) { mode, blacklisted, whitelisted, query ->
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .map { info ->
                val isSelected = when (mode) {
                    FilteringMode.BLACKLIST -> info.packageName in blacklisted
                    FilteringMode.WHITELIST -> info.packageName in whitelisted
                }
                AppInfo(
                    packageName = info.packageName,
                    appName = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info),
                    isSelected = isSelected
                )
            }
            .filter { it.appName.contains(query, ignoreCase = true) }
            .sortedBy { it.appName }

        AppListState(installedApps, mode, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppListState())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(packageName: String) {
        viewModelScope.launch {
            val currentMode = preferenceManager.filteringMode.first()
            if (currentMode == FilteringMode.BLACKLIST) {
                val current = preferenceManager.blacklistedApps.first().toMutableSet()
                if (packageName in current) current.remove(packageName) else current.add(packageName)
                preferenceManager.updateBlacklistedApps(current)
            } else {
                val current = preferenceManager.whitelistedApps.first().toMutableSet()
                if (packageName in current) current.remove(packageName) else current.add(packageName)
                preferenceManager.updateWhitelistedApps(current)
            }
        }
    }
}
