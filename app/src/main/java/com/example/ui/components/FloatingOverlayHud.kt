package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.MacroStepItem
import com.example.service.FloatingOrbService
import com.example.ui.viewmodel.LuminaViewModel
import kotlin.math.roundToInt

@Composable
fun FloatingOverlayHud(
    viewModel: LuminaViewModel,
    onOpenPermissions: () -> Unit,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val persona by viewModel.currentPersona.collectAsState()
    val orbTheme by viewModel.currentOrbTheme.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isRecordingMacro by viewModel.isRecordingMacro.collectAsState()
    val recordedSteps by viewModel.recordedSteps.collectAsState()
    val recordingName by viewModel.currentRecordingName.collectAsState()
    val replayStatus by viewModel.macroReplayStatus.collectAsState()

    var offsetX by remember { mutableFloatStateOf(20f) }
    var offsetY by remember { mutableFloatStateOf(160f) }
    var isExpanded by remember { mutableStateOf(false) }
    var isSystemOverlayActive by remember { mutableStateOf(FloatingOrbService.isRunning) }

    val primaryColor = Color(persona.primaryColorHex)
    val accentColor = Color(persona.accentColorHex)

    Box(modifier = modifier.fillMaxSize()) {

        // 1. Draggable Floating Widget
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, 850f)
                        offsetY = (offsetY + dragAmount.y).coerceIn(50f, 1800f)
                    }
                }
        ) {
            if (isRecordingMacro) {
                // Floating Macro Recording Toolbar HUD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0F2A)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF007F)),
                    modifier = Modifier
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE MACRO: ${recordingName.take(12)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF80AB)
                                )
                            }
                            Text(
                                text = "Step ${recordedSteps.size}/1",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Macro Step Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Touch Tap Step
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF331644))
                                    .clickable {
                                        viewModel.addMacroStep(
                                            MacroStepItem(
                                                stepNumber = recordedSteps.size + 1,
                                                title = "Tap Screen Center",
                                                actionType = ActionType.GESTURE_TAP,
                                                xCoord = 540,
                                                yCoord = 960,
                                                delayMs = 1000
                                            )
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ Tap", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Swipe Up Step
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF331644))
                                    .clickable {
                                        viewModel.addMacroStep(
                                            MacroStepItem(
                                                stepNumber = recordedSteps.size + 1,
                                                title = "Swipe Up (Scroll)",
                                                actionType = ActionType.GESTURE_SWIPE,
                                                xCoord = 540,
                                                yCoord = 1600,
                                                delayMs = 2500
                                            )
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SwipeVertical, contentDescription = null, tint = Color(0xFFFF80AB), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ Swipe", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Text Variable Step
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF331644))
                                    .clickable {
                                        viewModel.addMacroStep(
                                            MacroStepItem(
                                                stepNumber = recordedSteps.size + 1,
                                                title = "Input Variable",
                                                actionType = ActionType.ACCESSIBILITY_NODE,
                                                textPayload = "\${INPUT_${recordedSteps.size + 1}}",
                                                variableName = "INPUT_${recordedSteps.size + 1}",
                                                delayMs = 800
                                            )
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ Var", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Save & Finish Step
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00E676))
                                    .clickable { viewModel.saveRecordedMacro() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Save", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            } else {
                // Normal Floating Orb Icon
                Box {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        primaryColor,
                                        accentColor,
                                        Color(0xFF070A12)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(listOf(primaryColor, Color(orbTheme.ringColor), primaryColor)),
                                shape = CircleShape
                            )
                            .shadow(12.dp, CircleShape)
                            .clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Listening",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (isSpeaking) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Speaking",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = persona.displayName.take(1),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    // Mini Close Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222B40))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close HUD",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }

        // 2. Floating Quick HUD Flyout Menu
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1426).copy(alpha = 0.95f)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF223055)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LUMINA / MAYA 3.0 FLOATING CONTROLLER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { isExpanded = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Floating Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Speak Trigger
                        Button(
                            onClick = {
                                isExpanded = false
                                if (isListening) viewModel.stopVoiceInput() else viewModel.startVoiceInput()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Floating Macro Quick Record
                        Button(
                            onClick = {
                                isExpanded = false
                                viewModel.startMacroRecording("Floating_Macro_${System.currentTimeMillis() % 1000}")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Screen Vision Scan
                        Button(
                            onClick = {
                                isExpanded = false
                                viewModel.sendMessage("Scan current screen and summarize UI state")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vision", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // System Overlay Service Toggle & Permission Hub
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF141C30))
                            .border(1.dp, Color(0xFF263558), RoundedCornerShape(10.dp))
                            .clickable {
                                if (Settings.canDrawOverlays(context)) {
                                    if (isSystemOverlayActive) {
                                        FloatingOrbService.stop(context)
                                        isSystemOverlayActive = false
                                    } else {
                                        FloatingOrbService.start(context)
                                        isSystemOverlayActive = true
                                    }
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                    context.startActivity(intent)
                                }
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Home Screen Floating Overlay",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (Settings.canDrawOverlays(context)) "Permission Granted • Tap to toggle service" else "Needs 'Display Over Other Apps' permission",
                                fontSize = 10.sp,
                                color = if (Settings.canDrawOverlays(context)) Color(0xFF00E676) else Color(0xFFFF5252)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (Settings.canDrawOverlays(context)) Color(0xFF00E676) else Color(0xFFFF9100))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (Settings.canDrawOverlays(context)) "ACTIVE" else "GRANT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // One-Click Permission Master Setup Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E2840))
                            .clickable { onOpenPermissions() }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Permission Master Hub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                        }
                        Text("Grant All →", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}
