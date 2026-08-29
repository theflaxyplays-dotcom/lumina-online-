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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.OrbTheme
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun SettingsScreen(
    viewModel: LuminaViewModel,
    onOpenPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentOrbTheme.collectAsState()

    // Provider 1: Gemini
    val customKey by viewModel.customGeminiApiKey.collectAsState()
    val geminiModel by viewModel.geminiModelName.collectAsState()
    val geminiTestResult by viewModel.geminiTestResult.collectAsState()

    // Provider 2: Groq
    val groqKey by viewModel.groqApiKey.collectAsState()
    val groqModel by viewModel.groqModelName.collectAsState()
    val groqTestResult by viewModel.groqTestResult.collectAsState()

    // Provider 3: NVIDIA NIM
    val nvidiaKey by viewModel.nvidiaApiKey.collectAsState()
    val nvidiaModel by viewModel.nvidiaModelName.collectAsState()
    val nvidiaTestResult by viewModel.nvidiaTestResult.collectAsState()

    val isTestingApi by viewModel.isTestingApi.collectAsState()

    var geminiKeyInput by remember { mutableStateOf(customKey) }
    var geminiModelInput by remember { mutableStateOf(geminiModel) }
    var geminiSavedToast by remember { mutableStateOf(false) }

    var groqKeyInput by remember { mutableStateOf(groqKey) }
    var groqModelInput by remember { mutableStateOf(groqModel) }
    var groqSavedToast by remember { mutableStateOf(false) }

    var nvidiaKeyInput by remember { mutableStateOf(nvidiaKey) }
    var nvidiaModelInput by remember { mutableStateOf(nvidiaModel) }
    var nvidiaSavedToast by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title Header
        Text(
            text = "LUMINA MULTI-PROVIDER AI HUB",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color.White
        )
        Text(
            text = "OpenRouter-Style Multi-LLM Fallback Engine & System Setup",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Permission Master Center Launcher Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenPermissions() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2E)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PERMISSION MASTER WIZARD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "Accessibility, Overlay, WhatsApp & Battery",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF00E5FF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("CONFIGURE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Tier Fallback Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E4E))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MULTI-LLM CASCADE CHAIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E5FF)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE FAILOVER", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    }
                }

                Text(
                    text = "Tier 1: Google Gemini API (Primary)\n" +
                            "  ↓ (On 429/503 error)\n" +
                            "Tier 2: Groq Cloud (Ultra-Fast 500 T/s)\n" +
                            "  ↓ (On 429/503 error)\n" +
                            "Tier 3: NVIDIA NIM (NVIDIA Cloud)\n" +
                            "  ↓ (If all offline)\n" +
                            "Tier 4: Zero-Latency Edge Smart Action Engine",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 1. PROVIDER: GOOGLE GEMINI API
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E4C))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROVIDER 1: GOOGLE GEMINI API",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Google AI Studio Generative Language REST API endpoint:",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("API Key:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = geminiKeyInput,
                    onValueChange = { geminiKeyInput = it },
                    placeholder = { Text("AIzaSy... (or default from env)", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Model Name:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = geminiModelInput,
                    onValueChange = { geminiModelInput = it },
                    placeholder = { Text("gemini-3.5-flash / gemini-flash-latest", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-pro-preview").forEach { chipModel ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (geminiModelInput == chipModel) Color(0xFF00E5FF) else Color(0xFF162238))
                                .clickable { geminiModelInput = chipModel }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = chipModel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (geminiModelInput == chipModel) Color.Black else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (geminiTestResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = geminiTestResult ?: "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (geminiTestResult?.startsWith("✓") == true) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.setGeminiConfig(geminiKeyInput, geminiModelInput)
                            geminiSavedToast = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.testGeminiApiKey(geminiKeyInput, geminiModelInput) },
                        enabled = !isTestingApi,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF223254), contentColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isTestingApi) "Testing..." else "Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (geminiSavedToast) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✓ Gemini configuration saved permanently!", fontSize = 11.sp, color = Color(0xFF00E676))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 2. PROVIDER: GROQ CLOUD (LLaMA-3.3)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332042))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROVIDER 2: GROQ CLOUD (ULTRA-FAST)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4081)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "High-speed LPU inference (e.g. LLaMA-3.3-70B, Mixtral):",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Groq API Key:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = groqKeyInput,
                    onValueChange = { groqKeyInput = it },
                    placeholder = { Text("gsk_...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF4081),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Model Name:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = groqModelInput,
                    onValueChange = { groqModelInput = it },
                    placeholder = { Text("llama-3.3-70b-versatile / llama-3.1-8b-instant", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF4081),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768").forEach { chipModel ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (groqModelInput == chipModel) Color(0xFFFF4081) else Color(0xFF162238))
                                .clickable { groqModelInput = chipModel }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = chipModel.take(15),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (groqModelInput == chipModel) Color.White else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (groqTestResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = groqTestResult ?: "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (groqTestResult?.startsWith("✓") == true) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.setGroqConfig(groqKeyInput, groqModelInput)
                            groqSavedToast = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Groq", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.testGroqApiKey(groqKeyInput, groqModelInput) },
                        enabled = !isTestingApi,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E32), contentColor = Color(0xFFFF4081)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isTestingApi) "Testing..." else "Test Groq", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (groqSavedToast) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✓ Groq configuration saved permanently!", fontSize = 11.sp, color = Color(0xFF00E676))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 3. PROVIDER: NVIDIA NIM API
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A20))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFF76B900), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROVIDER 3: NVIDIA NIM (NVIDIA CLOUD)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF76B900)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enterprise AI microservices at integrate.api.nvidia.com:",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("NVIDIA API Key:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = nvidiaKeyInput,
                    onValueChange = { nvidiaKeyInput = it },
                    placeholder = { Text("nvapi-...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF76B900),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Model Name:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = nvidiaModelInput,
                    onValueChange = { nvidiaModelInput = it },
                    placeholder = { Text("meta/llama-3.1-70b-instruct / nvidia/llama-3.1-nemotron-70b-instruct", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF76B900),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("meta/llama-3.1-70b-instruct", "nvidia/llama-3.1-nemotron-70b-instruct").forEach { chipModel ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (nvidiaModelInput == chipModel) Color(0xFF76B900) else Color(0xFF162238))
                                .clickable { nvidiaModelInput = chipModel }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = chipModel.substringAfter("/").take(16),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nvidiaModelInput == chipModel) Color.Black else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (nvidiaTestResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = nvidiaTestResult ?: "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (nvidiaTestResult?.startsWith("✓") == true) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.setNvidiaConfig(nvidiaKeyInput, nvidiaModelInput)
                            nvidiaSavedToast = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF76B900), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save NVIDIA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.testNvidiaApiKey(nvidiaKeyInput, nvidiaModelInput) },
                        enabled = !isTestingApi,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2E1E), contentColor = Color(0xFF76B900)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isTestingApi) "Testing..." else "Test NVIDIA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (nvidiaSavedToast) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✓ NVIDIA configuration saved permanently!", fontSize = 11.sp, color = Color(0xFF00E676))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MQTT ESP32 Smart Home Gateway Card
        val mqttStatus by viewModel.mqttConnectionStatus.collectAsState()
        val mqttLog by viewModel.mqttLastLog.collectAsState()
        var mqttBrokerUrl by remember { mutableStateOf("tcp://broker.emqx.io:1883") }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E4C))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MQTT IOT / ESP32 GATEWAY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (mqttStatus) {
                                    com.example.data.model.MqttConnectionStatus.CONNECTED -> Color(0xFF00E676)
                                    com.example.data.model.MqttConnectionStatus.CONNECTING -> Color(0xFFFFD600)
                                    com.example.data.model.MqttConnectionStatus.ERROR -> Color(0xFFFF5252)
                                    else -> Color.Gray
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mqttStatus.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Controls real ESP32 smart home devices over MQTT broker:",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mqttBrokerUrl,
                    onValueChange = { mqttBrokerUrl = it },
                    placeholder = { Text("tcp://broker.emqx.io:1883", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF2B3A60),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.connectMqttBroker(mqttBrokerUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Connect Broker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (mqttLog.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Log: $mqttLog",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8 Selectable Orb Themes
        Text(
            text = "HOLOGRAPHIC ORB THEMES (8 STYLES)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OrbTheme.values().forEach { theme ->
                val isSelected = theme == currentTheme
                val primary = Color(theme.primaryColor)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) primary.copy(alpha = 0.2f) else Color(0xFF0F1524))
                        .border(1.dp, if (isSelected) primary else Color(0xFF202A42), RoundedCornerShape(10.dp))
                        .clickable { viewModel.switchOrbTheme(theme) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(primary, Color(theme.secondaryColor))))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = theme.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = theme.description,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Architecture Specs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1322)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2840))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "LUMINA ZERO-MOCK OS SPECS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF80AB)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Architect: Flaxy\n" +
                            "• Multi-LLM Tier: Gemini 2.5 Flash → Groq Cloud → NVIDIA NIM → Edge Engine\n" +
                            "• Voice Biometrics: MFCC + Cosine Similarity Raw Audio Verification\n" +
                            "• Gateway: Samsung Bixby & Google Assist Intent Integration\n" +
                            "• Engine: Android Dynamic Accessibility + MediaProjection + Notification RemoteInput\n" +
                            "• Database: Jetpack Room SQLite Persistence\n" +
                            "• Voice Synthesis: Multi-Persona Formant Modulation (Lumina / Friday / Venom)\n" +
                            "• Real-Time IoT: Eclipse Paho MQTT ESP32 Gateway",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
