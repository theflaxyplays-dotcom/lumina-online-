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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.AgentType
import com.example.data.model.SubAgentStatus
import com.example.data.model.SubAgentTask
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun SubAgentsScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.subAgentTasks.collectAsState()
    val smartDevices by viewModel.smartHomeDevices.collectAsState()

    var selectedAgentType by remember { mutableStateOf(AgentType.RESEARCHER) }
    var taskPromptInput by remember { mutableStateOf("Analyze top trending Android architectural patterns for 2026") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = "MINI-LUMINA SUB-AGENT SWARM",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color(0xFFD500F9)
        )
        Text(
            text = "Parallel AI Workers (Groq/Ollama/Mistral) & ESP32 Smart IoT Hub",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dispatch New Sub-Agent Task Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141022)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A1A6D))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DISPATCH NEW SUB-AGENT WORKER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEA80FC)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Agent Type Switcher Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AgentType.values().forEach { type ->
                        val isSelected = type == selectedAgentType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFD500F9).copy(alpha = 0.25f) else Color(0xFF1E1430))
                                .border(1.dp, if (isSelected) Color(0xFFD500F9) else Color(0xFF332050), RoundedCornerShape(8.dp))
                                .clickable { selectedAgentType = type }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (type) {
                                    AgentType.RESEARCHER -> "Research"
                                    AgentType.CODER -> "Coder"
                                    AgentType.DOC_GEN -> "PDF Gen"
                                    AgentType.IOT_HOME -> "Smart IoT"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFEA80FC) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = taskPromptInput,
                    onValueChange = { taskPromptInput = it },
                    label = { Text("Agent Objective / Prompt", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD500F9),
                        unfocusedBorderColor = Color(0xFF3B205C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (taskPromptInput.isNotBlank()) {
                            viewModel.spawnSubAgentTask(selectedAgentType, taskPromptInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD500F9)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch ${selectedAgentType.label.substringBefore(" ")}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Sub-Agent Swarm Tasks
        Text(
            text = "ACTIVE SWARM JOBS (${tasks.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEach { task ->
                SubAgentTaskCard(task = task)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ESP32 Smart Home Relay Switchboard
        Text(
            text = "ESP32 SMART HOME GATEWAY (MQTT)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF00E676)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            smartDevices.entries.take(2).forEach { entry ->
                SmartDeviceCard(
                    name = entry.key,
                    isOn = entry.value,
                    onToggle = { viewModel.toggleSmartHomeDevice(entry.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            smartDevices.entries.drop(2).take(2).forEach { entry ->
                SmartDeviceCard(
                    name = entry.key,
                    isOn = entry.value,
                    onToggle = { viewModel.toggleSmartHomeDevice(entry.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SubAgentTaskCard(task: SubAgentTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1424)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222B45))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (task.status == SubAgentStatus.COMPLETED) Color(0xFF00E676).copy(alpha = 0.2f)
                            else Color(0xFF00E5FF).copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.status.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (task.status == SubAgentStatus.COMPLETED) Color(0xFF00E676) else Color(0xFF00E5FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Prompt: \"${task.prompt}\"",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { task.progressPercent / 100f },
                color = if (task.status == SubAgentStatus.COMPLETED) Color(0xFF00E676) else Color(0xFFD500F9),
                trackColor = Color(0xFF1E2638),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = task.outputResult,
                fontSize = 10.sp,
                color = Color(0xFF80D8FF),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SmartDeviceCard(
    name: String,
    isOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) Color(0xFF0F2B1D) else Color(0xFF101624)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOn) Color(0xFF00E676) else Color(0xFF202A40)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = if (isOn) Color(0xFF00E676) else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOn) Color(0xFF00E676) else Color(0xFF424242))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (isOn) "POWER ON" else "STANDBY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (isOn) Color(0xFF00E676) else Color.White.copy(alpha = 0.4f)
            )
        }
    }
}
