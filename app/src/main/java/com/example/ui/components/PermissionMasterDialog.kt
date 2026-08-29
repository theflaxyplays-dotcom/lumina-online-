package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.service.LuminaAccessibilityService

@Composable
fun PermissionMasterDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }

    // Runtime Permission Request Launchers
    val requestAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshTrigger++ }

    val requestCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshTrigger++ }

    val requestPhoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshTrigger++ }

    val requestNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshTrigger++ }

    // Live permission checks
    val isAccessibilityGranted = LuminaAccessibilityService.isServiceActive.value
    val isOverlayGranted = Settings.canDrawOverlays(context)
    val isNotificationListenerGranted = isNotificationServiceEnabled(context)
    val isBatteryOptimizedIgnored = isBatteryOptimizationIgnored(context)
    val isMicGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val isCameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val isPhoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1322)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263558))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PERMISSION MASTER HUB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    text = "Grant these permissions to give Lumina full autonomous OS control and home screen macro powers.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Permission Item 1: Accessibility Service (OS Control)
                PermissionRowCard(
                    title = "1. Accessibility Service (Full OS Control)",
                    description = "Required for clicking buttons, scrolling reels, typing inputs, and running macros.",
                    isGranted = isAccessibilityGranted,
                    icon = Icons.Default.TouchApp,
                    accentColor = Color(0xFF00E5FF),
                    onGrantClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission Item 2: Display Over Other Apps (Floating Orb)
                PermissionRowCard(
                    title = "2. Display Over Other Apps (Floating Orb)",
                    description = "Enables the draggable floating assistant orb & live macro toolbar over any app.",
                    isGranted = isOverlayGranted,
                    icon = Icons.Default.Layers,
                    accentColor = Color(0xFFFF4081),
                    onGrantClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission Item 3: Notification Listener (WhatsApp Direct Reply)
                PermissionRowCard(
                    title = "3. Notification Listener (WhatsApp Auto-Reply)",
                    description = "Allows Lumina to intercept WhatsApp chats and send AI direct replies in background.",
                    isGranted = isNotificationListenerGranted,
                    icon = Icons.Default.NotificationsActive,
                    accentColor = Color(0xFF25D366),
                    onGrantClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission Item 4: Battery Optimization Exemption
                PermissionRowCard(
                    title = "4. Unrestricted Battery (24/7 Background Run)",
                    description = "Prevents Android from killing Lumina when phone is locked or in standby.",
                    isGranted = isBatteryOptimizedIgnored,
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = Color(0xFFFFD600),
                    onGrantClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(fallback)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission Item 5: Microphone (Voice Interaction)
                PermissionRowCard(
                    title = "5. Microphone (Voice Wake & Commands)",
                    description = "Continuous voice listening and persona conversation.",
                    isGranted = isMicGranted,
                    icon = Icons.Default.Mic,
                    accentColor = Color(0xFF7C4DFF),
                    onGrantClick = {
                        requestAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission Item 6: Camera & Screen Vision
                PermissionRowCard(
                    title = "6. Camera & Screen Vision",
                    description = "Game frame co-casting, screen scanning, and object analysis.",
                    isGranted = isCameraGranted,
                    icon = Icons.Default.CameraAlt,
                    accentColor = Color(0xFF00E676),
                    onGrantClick = {
                        requestCameraLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission Item 7: Phone & Emergency SOS
                PermissionRowCard(
                    title = "7. Phone Calls & Emergency SOS",
                    description = "Direct SIM dialing and automated emergency SMS transmission.",
                    isGranted = isPhoneGranted,
                    icon = Icons.Default.Call,
                    accentColor = Color(0xFFFF1744),
                    onGrantClick = {
                        requestPhoneLauncher.launch(Manifest.permission.CALL_PHONE)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done & Return to Assistant", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PermissionRowCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    accentColor: Color,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isGranted) Color(0xFF101B2E) else Color(0xFF1B1424))
            .border(1.dp, if (isGranted) accentColor.copy(alpha = 0.4f) else Color(0xFF33203A), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) accentColor else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF00E676).copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("GRANTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
            }
        } else {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("GRANT", fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(pkgName)
}

private fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    } else {
        true
    }
}
