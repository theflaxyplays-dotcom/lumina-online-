package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GamingEvent
import com.example.data.model.GamingEventType
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun GamingVisionScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val isVisionActive by viewModel.isGamingVisionActive.collectAsState()
    val gamingEvents by viewModel.gamingEvents.collectAsState()
    val persona by viewModel.currentPersona.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    val primaryColor = Color(persona.primaryColorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BGMI LIVE CO-CASTER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color(0xFFFF9100)
                )
                Text(
                    text = "Real-Time Esports Voice Commentary & Tactical Vision Feed",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Button(
                onClick = { viewModel.toggleGamingVision() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVisionActive) Color(0xFFFF1744) else Color(0xFF00E676),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isVisionActive) Icons.Default.FiberManualRecord else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isVisionActive) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isVisionActive) "LIVE ON AIR" else "START CASTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Vision HUD Scanner Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1420)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isVisionActive) Color(0xFFFF9100) else Color(0xFF23304A)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isVisionActive) Color(0xFFFF1744) else Color(0xFF757575))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isVisionActive) "VISION TELEMETRY: 60 FPS ACTIVE" else "VISION TELEMETRY: STANDBY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isVisionActive) Color(0xFFFF9100) else Color.White.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = "VOICE: ${persona.displayName.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Audio Wave Visualizer
                AudioWaveVisualizer(
                    isActive = isSpeaking || isVisionActive,
                    primaryColor = Color(0xFFFF9100),
                    secondaryColor = Color(0xFFFF1744),
                    modifier = Modifier.fillMaxWidth(),
                    barCount = 28,
                    maxHeight = 24.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Lumina dynamically crawls active frames, predicts enemy rotations, calls out airdrops, and narrates clutches with ultra-low latency.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Co-Caster Simulation Trigger Grid
        Text(
            text = "TRIGGER REAL-TIME CO-CASTER EVENTS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { viewModel.triggerSimulatedGamingEvent(GamingEventType.ENEMY_SPOTTED) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33161C)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🎯 Enemy Spot", fontSize = 10.sp, color = Color(0xFFFF5252))
            }

            Button(
                onClick = { viewModel.triggerSimulatedGamingEvent(GamingEventType.AIRDROP) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332B10)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("📦 Airdrop", fontSize = 10.sp, color = Color(0xFFFFD600))
            }

            Button(
                onClick = { viewModel.triggerSimulatedGamingEvent(GamingEventType.CLUTCH) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF301033)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🔥 1v4 Clutch", fontSize = 10.sp, color = Color(0xFFFF4081))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Commentary Feed
        Text(
            text = "LIVE ESPORTS COMMENTARY FEED (${gamingEvents.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gamingEvents, key = { it.id }) { event ->
                GamingEventCard(
                    event = event,
                    persona = persona,
                    onSpeakAgain = {
                        viewModel.speakMessage(
                            com.example.data.model.ChatMessage(
                                sender = com.example.data.model.MessageSender.LUMINA,
                                text = event.voiceCommentary,
                                persona = persona
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun GamingEventCard(
    event: GamingEvent,
    persona: com.example.data.model.LuminaPersona,
    onSpeakAgain: () -> Unit
) {
    val badgeColor = Color(event.eventType.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.eventType.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = onSpeakAgain, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak",
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🎙️ \"${event.voiceCommentary}\"",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hype Score: ${event.hypeScore}/100",
                    fontSize = 9.sp,
                    color = Color(0xFFFFD600),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Voice Tone: ${persona.displayName}",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
