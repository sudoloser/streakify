package dev.sudoloser.streakify.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sudoloser.streakify.data.prefs.FilteringMode
import dev.sudoloser.streakify.data.prefs.PreferenceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val filteringMode: FilteringMode = FilteringMode.BLACKLIST,
    val materialYou: Boolean = true,
    val reminderThreshold: Int = 1
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        preferenceManager.filteringMode,
        preferenceManager.materialYou,
        preferenceManager.reminderThreshold
    ) { mode, materialYou, threshold ->
        SettingsState(mode, materialYou, threshold)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun setFilteringMode(mode: FilteringMode) {
        viewModelScope.launch { preferenceManager.setFilteringMode(mode) }
    }

    fun setMaterialYou(enabled: Boolean) {
        viewModelScope.launch { preferenceManager.setMaterialYou(enabled) }
    }

    fun setReminderThreshold(threshold: Int) {
        viewModelScope.launch { preferenceManager.setReminderThreshold(threshold) }
    }
}
