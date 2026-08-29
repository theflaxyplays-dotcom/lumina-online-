package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun GuardianScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val isGuardModeActive by viewModel.isGuardModeActive.collectAsState()
    val isIncognitoActive by viewModel.isIncognitoActive.collectAsState()
    val enrollmentStep by viewModel.voiceEnrollmentStep.collectAsState()
    val voiceSimilarity by viewModel.voiceSimilarity.collectAsState()
    val isVoiceEnrolled by viewModel.isVoiceEnrolled.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
    val biometricsLog by viewModel.biometricsLog.collectAsState()

    var similaritySlider by remember { mutableStateOf(0.75f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title Header
        Text(
            text = "VOICE BIOMETRICS GUARDIAN",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color(0xFF00E5FF)
        )
        Text(
            text = "Real MFCC Vector Extraction, Cosine Similarity & Anti-Theft Lock",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Guard Mode Active Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isGuardModeActive) Color(0xFF0D2530) else Color(0xFF141A28)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isGuardModeActive) Color(0xFF00E5FF) else Color(0xFF263350)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isGuardModeActive) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGuardModeActive) "GUARD MODE ENGAGED" else "GUARD MODE STANDBY",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (isGuardModeActive) Color(0xFF00E5FF) else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isGuardModeActive)
                            "Lockdown enabled. Audio input is analyzed against your 20-dim MFCC voice vector. Unauthorized speakers trigger security alarm."
                        else
                            "Enable to enforce voice biometric verification before executing system actions or accessibility taps.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Switch(
                    checked = isGuardModeActive,
                    onCheckedChange = { viewModel.toggleGuardMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real Voice Biometric Enrollment Status (3-Step Calibration)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222F4C))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF80D8FF), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3-STEP MFCC VOICE CALIBRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF80D8FF)
                        )
                    }

                    if (isVoiceEnrolled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("FLAXY CALIBRATED", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val samplePhrases = listOf(
                    "Step 1: \"Lumina, verify Flaxy voice signature.\"",
                    "Step 2: \"Execute autonomous phone protocol.\"",
                    "Step 3: \"Flaxy standing by for command sequence.\""
                )

                samplePhrases.forEachIndexed { index, phrase ->
                    val isDone = index < enrollmentStep
                    val isCurrent = index == enrollmentStep

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDone) Color(0xFF0D251A) else if (isCurrent) Color(0xFF1E2840) else Color(0xFF101624))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = phrase,
                                fontSize = 11.sp,
                                color = if (isDone) Color(0xFF00E676) else Color.White
                            )
                        }

                        if (isDone) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        } else if (isCurrent) {
                            Button(
                                onClick = { viewModel.startVoiceCalibration() },
                                enabled = !isVoiceRecording,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                if (isVoiceRecording) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Text("Record Sample", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time MFCC Engine Log & Reset Button
                if (biometricsLog.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF141C2E))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = biometricsLog,
                            fontSize = 10.sp,
                            color = Color(0xFF80D8FF),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Cosine Similarity Match: ${(voiceSimilarity * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (voiceSimilarity >= 0.70f) Color(0xFF00E676) else Color(0xFFFF5252)
                    )

                    if (isVoiceEnrolled || enrollmentStep > 0) {
                        Button(
                            onClick = { viewModel.resetVoiceCalibration() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF241620), contentColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cosine Similarity Threshold Slider
                Text(
                    text = "Acceptance Threshold: ${(similaritySlider * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Slider(
                    value = similaritySlider,
                    onValueChange = { similaritySlider = it },
                    valueRange = 0.50f..0.95f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Incognito Mode Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222F4C))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INCOGNITO AUTOMATION MODE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Disables all persistent logging of chat and macro executions to Room DB.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Switch(
                    checked = isIncognitoActive,
                    onCheckedChange = { viewModel.toggleIncognito() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF80AB)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Intruder Alarm Test Button
        Button(
            onClick = { viewModel.triggerIntruderAlertSimulated() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("TRIGGER SECURITY INTRUDER ALARM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
