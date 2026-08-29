package com.example

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ScreenCaptureService
import com.example.ui.components.FloatingOverlayHud
import com.example.ui.components.PermissionMasterDialog
import com.example.ui.components.ScreenShareConfirmDialog
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.ChatConsoleScreen
import com.example.ui.screens.GamingVisionScreen
import com.example.ui.screens.GuardianScreen
import com.example.ui.screens.MacroEngineScreen
import com.example.ui.screens.OrbHomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubAgentsScreen
import com.example.ui.screens.WhatsAppHubScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LuminaViewModel

enum class LuminaTab(
    val title: String,
    val icon: ImageVector
) {
    ORB_HOME("Orb Core", Icons.Default.RadioButtonChecked),
    CONSOLE("Console", Icons.AutoMirrored.Filled.Chat),
    AUTOMATION("OS Control", Icons.Default.TouchApp),
    MACROS("Macros", Icons.Default.Layers),
    WHATSAPP("DirectReply", Icons.Default.Notifications),
    GAMING("Co-Caster", Icons.Default.Campaign),
    GUARDIAN("Guardian", Icons.Default.Security),
    SUB_AGENTS("Swarm", Icons.Default.Hub),
    SETTINGS("Setup Hub", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: LuminaViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted && (intent?.action == Intent.ACTION_ASSIST || intent?.action == Intent.ACTION_VOICE_COMMAND)) {
            viewModel.startVoiceInput()
        }
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            viewModel.onScreenProjectionGranted()
        } else {
            viewModel.onScreenProjectionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request essential runtime permissions on launch
        val permissionsToRequest = mutableListOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())

        // Handle Bixby / Assistant voice trigger invocation
        if (intent?.action == Intent.ACTION_ASSIST || intent?.action == Intent.ACTION_VOICE_COMMAND) {
            viewModel.startVoiceInput()
        }

        viewModel.requestMediaProjectionLauncher = {
            try {
                val mpManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjectionLauncher.launch(mpManager.createScreenCaptureIntent())
            } catch (e: Exception) {
                viewModel.analyzeScreenVision()
            }
        }

        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableStateOf(LuminaTab.ORB_HOME) }
                var showPermissionsDialog by remember { mutableStateOf(false) }
                val persona by viewModel.currentPersona.collectAsState()
                val primaryColor = Color(persona.primaryColorHex)
                val screenSharePrompt by viewModel.screenSharePrompt.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070A12)),
                    bottomBar = {
                        LuminaBottomNavBar(
                            currentTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            accentColor = primaryColor
                        )
                    },
                    containerColor = Color(0xFF070A12)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            LuminaTab.ORB_HOME -> OrbHomeScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { selectedTab = LuminaTab.CONSOLE },
                                onNavigateToMacros = { selectedTab = LuminaTab.MACROS },
                                onNavigateToGaming = { selectedTab = LuminaTab.GAMING },
                                onNavigateToAutomation = { selectedTab = LuminaTab.AUTOMATION },
                                onOpenPermissions = { showPermissionsDialog = true }
                            )
                            LuminaTab.CONSOLE -> ChatConsoleScreen(viewModel = viewModel)
                            LuminaTab.AUTOMATION -> AutomationScreen(viewModel = viewModel)
                            LuminaTab.MACROS -> MacroEngineScreen(viewModel = viewModel)
                            LuminaTab.WHATSAPP -> WhatsAppHubScreen(viewModel = viewModel)
                            LuminaTab.GAMING -> GamingVisionScreen(viewModel = viewModel)
                            LuminaTab.GUARDIAN -> GuardianScreen(viewModel = viewModel)
                            LuminaTab.SUB_AGENTS -> SubAgentsScreen(viewModel = viewModel)
                            LuminaTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onOpenPermissions = { showPermissionsDialog = true }
                            )
                        }

                        // Interactive Draggable Floating Overlay HUD Simulator (toggleable)
                        val isFloatingHudVisible by viewModel.isFloatingHudVisible.collectAsState()
                        if (isFloatingHudVisible) {
                            FloatingOverlayHud(
                                viewModel = viewModel,
                                onOpenPermissions = { showPermissionsDialog = true },
                                onClose = { viewModel.toggleFloatingHud(false) }
                            )
                        }

                        // Interactive Screen Share / Vision Permission Confirmation Dialog (Voice & Touch)
                        if (screenSharePrompt != null) {
                            ScreenShareConfirmDialog(
                                persona = persona,
                                requestedReason = screenSharePrompt ?: "",
                                onConfirm = { viewModel.confirmScreenShare() },
                                onDeny = { viewModel.rejectScreenShare() }
                            )
                        }

                        // Full System Permission Master Dialog
                        if (showPermissionsDialog) {
                            PermissionMasterDialog(
                                onDismiss = { showPermissionsDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LuminaBottomNavBar(
    currentTab: LuminaTab,
    onTabSelected: (LuminaTab) -> Unit,
    accentColor: Color
) {
    val primaryTabs = listOf(
        LuminaTab.ORB_HOME,
        LuminaTab.CONSOLE,
        LuminaTab.AUTOMATION,
        LuminaTab.MACROS,
        LuminaTab.WHATSAPP,
        LuminaTab.GAMING,
        LuminaTab.SETTINGS
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF090D18))
            .border(width = 1.dp, color = Color(0xFF1B2338))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            primaryTabs.forEach { tab ->
                val isSelected = tab == currentTab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = tab.title,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
