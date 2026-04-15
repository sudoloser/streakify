package dev.sudoloser.streakify

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.sudoloser.streakify.data.prefs.PreferenceManager
import dev.sudoloser.streakify.ui.applist.AppListScreen
import dev.sudoloser.streakify.ui.dashboard.DashboardScreen
import dev.sudoloser.streakify.ui.settings.SettingsScreen
import dev.sudoloser.streakify.ui.theme.StreakifyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isMaterialYou by preferenceManager.materialYou.collectAsState(initial = true)
            var showPermissionDialog by remember { mutableStateOf(false) }

            StreakifyTheme(dynamicColor = isMaterialYou) {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    if (!hasUsageStatsPermission()) {
                        showPermissionDialog = true
                    }
                }

                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = { Text("Permission Needed") },
                        text = { Text("Streakify needs usage access to track which apps you use and manage your streaks. Please enable it in the next screen.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showPermissionDialog = false
                                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) {
                                Text("Open Settings")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPermissionDialog = false }) {
                                Text("Not Now")
                            }
                        }
                    )
                }

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            onSettingsClick = { navController.navigate("settings") },
                            onAppListClick = { navController.navigate("app_list") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable("app_list") {
                        AppListScreen(onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
