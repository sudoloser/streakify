package dev.sudoloser.streakify.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class FilteringMode {
    BLACKLIST, WHITELIST
}

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val FILTERING_MODE = stringPreferencesKey("filtering_mode")
        val MATERIAL_YOU = booleanPreferencesKey("material_you")
        val REMINDER_THRESHOLD = intPreferencesKey("reminder_threshold")
        val BLACKLISTED_APPS = stringSetPreferencesKey("blacklisted_apps")
        val WHITELISTED_APPS = stringSetPreferencesKey("whitelisted_apps")
        val CUSTOM_FONT_PATH = stringPreferencesKey("custom_font_path")
        val USE_SYSTEM_FONT = booleanPreferencesKey("use_system_font")
    }

    val filteringMode: Flow<FilteringMode> = context.dataStore.data.map {
        val modeName = it[Keys.FILTERING_MODE] ?: FilteringMode.BLACKLIST.name
        FilteringMode.valueOf(modeName)
    }

    suspend fun setFilteringMode(mode: FilteringMode) {
        context.dataStore.edit { it[Keys.FILTERING_MODE] = mode.name }
    }

    val materialYou: Flow<Boolean> = context.dataStore.data.map { it[Keys.MATERIAL_YOU] ?: true }

    suspend fun setMaterialYou(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MATERIAL_YOU] = enabled }
    }

    val reminderThreshold: Flow<Int> = context.dataStore.data.map { it[Keys.REMINDER_THRESHOLD] ?: 1 }

    suspend fun setReminderThreshold(threshold: Int) {
        context.dataStore.edit { it[Keys.REMINDER_THRESHOLD] = threshold }
    }

    val blacklistedApps: Flow<Set<String>> = context.dataStore.data.map { it[Keys.BLACKLISTED_APPS] ?: emptySet() }

    suspend fun updateBlacklistedApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.BLACKLISTED_APPS] = apps }
    }

    val whitelistedApps: Flow<Set<String>> = context.dataStore.data.map { it[Keys.WHITELISTED_APPS] ?: emptySet() }

    suspend fun updateWhitelistedApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.WHITELISTED_APPS] = apps }
    }
}
