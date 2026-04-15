package dev.sudoloser.streakify.ui.applist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.sudoloser.streakify.data.prefs.FilteringMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (state.filteringMode == FilteringMode.BLACKLIST) "Blacklist" else "Whitelist", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(state.apps) { app ->
                ListItem(
                    headlineContent = { Text(app.appName) },
                    supportingContent = { Text(app.packageName) },
                    leadingContent = {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(app.packageName)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    trailingContent = {
                        Checkbox(
                            checked = app.isSelected,
                            onCheckedChange = { viewModel.toggleSelection(app.packageName) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.toggleSelection(app.packageName) }
                )
            }
        }
    }
}
