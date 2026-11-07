package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.SyncState
import com.example.ui.components.SyncProgressDialog
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SmartFeaturesScreen
import com.example.ui.screens.WatchFacesScreen
import com.example.ui.theme.*
import com.example.viewmodel.DuWatchViewModel

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    FACES("Watch Faces", Icons.Default.Palette, "tab_faces"),
    ALARMS("Alarms", Icons.Default.Alarm, "tab_alarms"),
    FEATURES("Control", Icons.Default.Tune, "tab_features")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (t: Throwable) {
            // Safe fallback for custom OEM window decorators
        }
        setContent {
            MyApplicationTheme {
                DuWatchApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuWatchApp(viewModel: DuWatchViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Request Bluetooth and Notification runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions granted callback
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        try {
            permissionLauncher.launch(permissions.toTypedArray())
        } catch (t: Throwable) {
            // Safe fallback
        }
    }

    LaunchedEffect(context) {
        try {
            viewModel.attachContext(context)
        } catch (t: Throwable) {
            // Ignore context attachment issues
        }
    }

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val device by viewModel.device.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    // Handle back button to return to dashboard if on other tabs
    BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
        currentTab = NavigationTab.DASHBOARD
    }

    // Modal Bluetooth Transfer Dialog
    SyncProgressDialog(
        syncState = syncState,
        onDismiss = { viewModel.dismissSyncState() }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Logo Badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "DuWatch Logo",
                                tint = BackgroundDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "DuWatch",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "PRO",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${device.name} • ${device.batteryPercent}% Battery",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Sync BLE Button
                    IconButton(
                        onClick = { viewModel.refreshTelemetry() },
                        modifier = Modifier.testTag("appbar_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = SurfaceBorderDark,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BackgroundDark,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                when (tab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToFaces = { currentTab = NavigationTab.FACES },
                        onNavigateToAlarms = { currentTab = NavigationTab.ALARMS },
                        onNavigateToFeatures = { currentTab = NavigationTab.FEATURES }
                    )
                    NavigationTab.FACES -> WatchFacesScreen(viewModel = viewModel)
                    NavigationTab.ALARMS -> AlarmsScreen(viewModel = viewModel)
                    NavigationTab.FEATURES -> SmartFeaturesScreen(viewModel = viewModel)
                }
            }
        }
    }
}
