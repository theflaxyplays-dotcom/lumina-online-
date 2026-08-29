package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LuminaPersona
import com.example.data.model.OrbTheme
import com.example.ui.components.ActionCard
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.components.LuminaHeaderBar
import com.example.ui.components.LuminaOrb
import com.example.ui.viewmodel.LuminaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrbHomeScreen(
    viewModel: LuminaViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToMacros: () -> Unit,
    onNavigateToGaming: () -> Unit,
    onNavigateToAutomation: () -> Unit,
    onOpenPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val persona by viewModel.currentPersona.collectAsState()
    val orbTheme by viewModel.currentOrbTheme.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isGenerating.collectAsState()
    val recognizedSpeech by viewModel.recognizedSpeechText.collectAsState()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val isGuardModeActive by viewModel.isGuardModeActive.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val lastAction by viewModel.lastEmittedAction.collectAsState()
    val macroReplayStatus by viewModel.macroReplayStatus.collectAsState()
    val isFloatingHudVisible by viewModel.isFloatingHudVisible.collectAsState()
    val isVisionActive by viewModel.isVisionActive.collectAsState()
    val visionAnalysisStatus by viewModel.visionAnalysisStatus.collectAsState()

    var quickInputText by remember { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }

    val primaryColor = Color(persona.primaryColorHex)
    val accentColor = Color(persona.accentColorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top HUD Header
        LuminaHeaderBar(
            currentPersona = persona,
            isAccessibilityActive = isAccessibilityActive,
            isGuardModeActive = isGuardModeActive,
            onSwitchPersonaClick = {
                val next = when (persona) {
                    LuminaPersona.LUMINA -> LuminaPersona.FRIDAY
                    LuminaPersona.FRIDAY -> LuminaPersona.VENOM
                    LuminaPersona.VENOM -> LuminaPersona.LUMINA
                }
                viewModel.switchPersona(next)
            }
        )

        // Permission Master Hub Quick Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF10192C))
                .border(1.dp, Color(0xFF1E2D4E), RoundedCornerShape(10.dp))
                .clickable { onOpenPermissions() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isAccessibilityActive) Color(0xFF00E676) else Color(0xFFFF9100),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAccessibilityActive) "OS Permissions: ACTIVE & READY" else "Setup Accessibility & Overlay Permissions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Text(
                text = "SETUP HUB →",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
        }

        // Persona Switcher Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LuminaPersona.values().forEach { p ->
                val isSelected = p == persona
                val pColor = Color(p.primaryColorHex)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) pColor.copy(alpha = 0.25f) else Color(0xFF131826))
                        .border(
                            1.dp,
                            if (isSelected) pColor else Color(0xFF262D42),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.switchPersona(p) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = p.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) pColor else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Quick Float Bubble Toggle
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isFloatingHudVisible) Color(0xFF7C4DFF).copy(alpha = 0.3f) else Color(0xFF131826))
                    .border(
                        1.dp,
                        if (isFloatingHudVisible) Color(0xFF7C4DFF) else Color(0xFF262D42),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.toggleFloatingHud() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isFloatingHudVisible) "★ Float ON" else "☆ Float",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFloatingHudVisible) Color(0xFFB388FF) else Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Dedicated Screen Share & Live Vision Interactive Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isVisionActive) Color(0xFF1A0A20) else Color(0xFF0F172A)
                )
                .border(
                    1.dp,
                    if (isVisionActive) Color(0xFFFF4081) else Color(0xFF00E5FF).copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (isVisionActive) {
                        viewModel.analyzeScreenVision("Analyze current screen in detail")
                    } else {
                        viewModel.requestScreenShare("User tapped Screen Share Vision Bar")
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isVisionActive) Color(0xFFFF1744).copy(alpha = 0.2f) else Color(0xFF00E5FF).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Screen Vision",
                            tint = if (isVisionActive) Color(0xFFFF5252) else Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isVisionActive) "🔴 LIVE SCREEN VISION ACTIVE" else "📸 SCREEN SHARE (AI VISION)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isVisionActive) Color(0xFFFF80AB) else Color(0xFF80D8FF)
                        )
                        Text(
                            text = if (isVisionActive) "Tap to re-analyze screen or ask Lumina" else "Tap to request AI screen viewing permission",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isVisionActive) Color(0xFFFF1744) else Color(0xFF00E5FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isVisionActive) "ANALYZE NOW" else "START VISION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Central Holographic Orb HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LuminaOrb(
                theme = orbTheme,
                isListening = isListening,
                isSpeaking = isSpeaking,
                isThinking = isThinking,
                size = 230.dp,
                onClick = {
                    if (isSpeaking) {
                        viewModel.stopSpeaking()
                    } else if (!isListening) {
                        viewModel.startVoiceInput()
                    } else {
                        viewModel.stopVoiceInput()
                    }
                }
            )
        }

        // Voice Waveform & Speech Status
        AudioWaveVisualizer(
            isActive = isListening || isSpeaking || isThinking,
            primaryColor = primaryColor,
            secondaryColor = accentColor,
            modifier = Modifier.padding(vertical = 4.dp),
            barCount = 20,
            maxHeight = 28.dp
        )

        // State Indicator Text
        Text(
            text = when {
                isThinking -> "⚡ Lumina Reasoning & Tool Synthesizing..."
                isListening -> "🎙️ Listening to Flaxy... Speak now"
                isSpeaking -> "🔊 Lumina is speaking..."
                else -> "Tap Orb or Mic to trigger dynamic voice control"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isListening || isSpeaking) primaryColor else Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        if (recognizedSpeech.isNotBlank() && isListening) {
            Text(
                text = "\"$recognizedSpeech\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Active Macro Replay Banner
        AnimatedVisibility(visible = macroReplayStatus != null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙️ ${macroReplayStatus ?: ""}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
        }

        // Last Emitted Action Card
        if (lastAction != null) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ActionCard(
                    action = lastAction!!,
                    onExecuteClick = { viewModel.executeAction(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Automation Chips Row (17 Maya/Hunter AI Capability Suites)
        Text(
            text = "17 AUTONOMOUS CAPABILITY SUITES (150+ ACTIONS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Calls & Dual-SIM
            QuickActionChip(
                label = "Call Mom (SIM 1)",
                icon = Icons.Default.Phone,
                color = Color(0xFF00E676).copy(alpha = 0.2f),
                textColor = Color(0xFF69F0AE),
                onClick = { viewModel.sendMessage("Call Mom on SIM 1") }
            )

            // 2. WhatsApp & Video
            QuickActionChip(
                label = "WhatsApp Riya",
                icon = Icons.Default.Chat,
                color = Color(0xFF25D366).copy(alpha = 0.2f),
                textColor = Color(0xFF69F0AE),
                onClick = { viewModel.sendMessage("WhatsApp Riya: good morning! Let's meet at 5.") }
            )

            // 3. Music & Spotify Memory
            QuickActionChip(
                label = "Play Arijit Singh",
                icon = Icons.Default.MusicNote,
                color = Color(0xFF1DB954).copy(alpha = 0.2f),
                textColor = Color(0xFF1ED760),
                onClick = { viewModel.sendMessage("Play some Arijit Singh on Spotify") }
            )

            // 4. Phone Control & Reels
            QuickActionChip(
                label = "Auto-Scroll Reels",
                icon = Icons.Default.PlayArrow,
                color = Color(0xFFE1306C).copy(alpha = 0.2f),
                textColor = Color(0xFFFF80AB),
                onClick = { viewModel.sendMessage("Open Instagram and scroll reels") }
            )

            // 5. Screen & Camera Vision
            QuickActionChip(
                label = "Look at Screen",
                icon = Icons.Default.Videocam,
                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                textColor = Color(0xFF80D8FF),
                onClick = { viewModel.requestScreenShare("Look at my screen requested") }
            )

            // 6. Alarms & Reminders
            QuickActionChip(
                label = "Alarm 7 AM",
                icon = Icons.Default.Notifications,
                color = Color(0xFFFFD600).copy(alpha = 0.2f),
                textColor = Color(0xFFFFEA00),
                onClick = { viewModel.sendMessage("Set an alarm for 7 AM tomorrow") }
            )

            // 7. Read Notifications
            QuickActionChip(
                label = "Read Notifications",
                icon = Icons.Default.Notifications,
                color = Color(0xFFFF9100).copy(alpha = 0.2f),
                textColor = Color(0xFFFFB74D),
                onClick = { viewModel.sendMessage("Read my unread notifications out loud") }
            )

            // 8. Files & Resume Doc
            QuickActionChip(
                label = "Make Resume (Word)",
                icon = Icons.Default.Description,
                color = Color(0xFF29B6F6).copy(alpha = 0.2f),
                textColor = Color(0xFF81D4FA),
                onClick = { viewModel.sendMessage("Make a modern Android developer resume in Word") }
            )

            // 9. Coding & Web Deploy
            QuickActionChip(
                label = "Build Website",
                icon = Icons.Default.Code,
                color = Color(0xFF7C4DFF).copy(alpha = 0.2f),
                textColor = Color(0xFFB388FF),
                onClick = { viewModel.sendMessage("Build a landing website for my cafe with online ordering") }
            )

            // 10. Location & Maps
            QuickActionChip(
                label = "Navigate Airport",
                icon = Icons.Default.Map,
                color = Color(0xFF00B0FF).copy(alpha = 0.2f),
                textColor = Color(0xFF80D8FF),
                onClick = { viewModel.sendMessage("Navigate to nearest international airport") }
            )

            // 11. Smart Home ESP32
            QuickActionChip(
                label = "Turn Off Lights",
                icon = Icons.Default.Home,
                color = Color(0xFF00E676).copy(alpha = 0.2f),
                textColor = Color(0xFFB9F6CA),
                onClick = { viewModel.sendMessage("Turn off bedroom light and set AC to 24°C") }
            )

            // 12. Whiteboard Study Mode
            QuickActionChip(
                label = "Study Whiteboard",
                icon = Icons.Default.School,
                color = Color(0xFFFF4081).copy(alpha = 0.2f),
                textColor = Color(0xFFFF80AB),
                onClick = { viewModel.sendMessage("Explain Pythagoras theorem on the study board like a tutor") }
            )

            // 13. Task Macros Routine
            QuickActionChip(
                label = "Morning Routine",
                icon = Icons.Default.Tune,
                color = Color(0xFFD500F9).copy(alpha = 0.2f),
                textColor = Color(0xFFEA80FC),
                onClick = { viewModel.sendMessage("Run my morning routine macro") }
            )

            // 14. Live Gaming Co-Caster
            QuickActionChip(
                label = "BGMI Co-Caster",
                icon = Icons.Default.Campaign,
                color = Color(0xFFFF5252).copy(alpha = 0.2f),
                textColor = Color(0xFFFF8A80),
                onClick = onNavigateToGaming
            )

            // 15. Mini-Maya Sub-Agents
            QuickActionChip(
                label = "Mini-Agent Swarm",
                icon = Icons.Default.Hub,
                color = Color(0xFF7C4DFF).copy(alpha = 0.2f),
                textColor = Color(0xFFB388FF),
                onClick = { viewModel.sendMessage("Spawn a background researcher to summarize latest tech news") }
            )

            // 16. Flashlight / Torch
            QuickActionChip(
                label = if (isTorchOn) "Torch ON" else "Torch Toggle",
                icon = Icons.Default.FlashOn,
                color = if (isTorchOn) Color(0xFFFFD600) else Color(0xFF1E2638),
                textColor = if (isTorchOn) Color.Black else Color.White,
                onClick = { viewModel.toggleTorch() }
            )

            // 17. Emergency SOS Guardian
            QuickActionChip(
                label = "Emergency SOS",
                icon = Icons.Default.Warning,
                color = Color(0xFFFF1744).copy(alpha = 0.25f),
                textColor = Color(0xFFFF5252),
                onClick = { viewModel.triggerSos() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Input Console Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1424)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262D42))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isListening) viewModel.stopVoiceInput() else viewModel.startVoiceInput()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (isListening) primaryColor else Color.White.copy(alpha = 0.7f)
                    )
                }

                OutlinedTextField(
                    value = quickInputText,
                    onValueChange = { quickInputText = it },
                    placeholder = {
                        Text(
                            text = "Ask Lumina or command phone action...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )

                IconButton(
                    onClick = {
                        if (quickInputText.isNotBlank()) {
                            viewModel.sendMessage(quickInputText)
                            quickInputText = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = primaryColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
