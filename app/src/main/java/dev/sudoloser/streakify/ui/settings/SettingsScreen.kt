package dev.sudoloser.streakify.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sudoloser.streakify.data.prefs.FilteringMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Filtering Mode", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilteringMode.values().forEach { mode ->
                    FilterChip(
                        selected = state.filteringMode == mode,
                        onClick = { viewModel.setFilteringMode(mode) },
                        label = { Text(mode.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Notification Threshold: ${state.reminderThreshold} days", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = state.reminderThreshold.toFloat(),
                onValueChange = { viewModel.setReminderThreshold(it.toInt()) },
                valueRange = 1f..30f,
                steps = 29
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Material You", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.materialYou,
                    onCheckedChange = { viewModel.setMaterialYou(it) }
                )
            }
        }
    }
}
