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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.ActionType
import com.example.data.model.MacroRoutine
import com.example.data.model.MacroStepItem
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun MacroEngineScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val macros by viewModel.savedMacros.collectAsState()
    val isRecording by viewModel.isRecordingMacro.collectAsState()
    val recordingName by viewModel.currentRecordingName.collectAsState()
    val recordedSteps by viewModel.recordedSteps.collectAsState()
    val replayStatus by viewModel.macroReplayStatus.collectAsState()

    var showNewMacroDialog by remember { mutableStateOf(false) }
    var newRoutineNameInput by remember { mutableStateOf("My Custom Routine") }
    var selectedMacroForReplay by remember { mutableStateOf<MacroRoutine?>(null) }
    var variableInputMap by remember { mutableStateOf(mapOf<String, String>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DYNAMIC MACRO ENGINE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                Text(
                    text = "Step-by-Step Runtime Automation & Variable Synthesis",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            if (!isRecording) {
                Button(
                    onClick = { showNewMacroDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Macro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Replay Banner
        if (replayStatus != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚡ $replayStatus",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Live Macro Recording HUD
        if (isRecording) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF21102A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4081))
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RECORDING: $recordingName",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFFFF80AB)
                            )
                        }
                        Text(
                            text = "Step ${recordedSteps.size}/${recordedSteps.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Perform phone actions or tap quick step buttons below to capture automation sequence.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step Capture Action Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addMacroStep(
                                    MacroStepItem(
                                        stepNumber = recordedSteps.size + 1,
                                        title = "Swipe Up (Scroll)",
                                        actionType = ActionType.GESTURE_SWIPE,
                                        xCoord = 540,
                                        yCoord = 1600,
                                        delayMs = 2000
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E4A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Swipe", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.addMacroStep(
                                    MacroStepItem(
                                        stepNumber = recordedSteps.size + 1,
                                        title = "Input Custom Text",
                                        actionType = ActionType.ACCESSIBILITY_NODE,
                                        textPayload = "\${USER_INPUT_${recordedSteps.size + 1}}",
                                        variableName = "USER_INPUT_${recordedSteps.size + 1}"
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E4A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Variable", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.addMacroStep(
                                    MacroStepItem(
                                        stepNumber = recordedSteps.size + 1,
                                        title = "Toggle Flashlight",
                                        actionType = ActionType.SYSTEM_SETTING,
                                        textPayload = "torch"
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E4A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Torch", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recorded Steps List
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        recordedSteps.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF160E20))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${step.stepNumber}. ${step.title}",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = step.actionType.name,
                                    fontSize = 9.sp,
                                    color = Color(0xFFFF80AB),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.undoLastMacroStep() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Undo", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.saveRecordedMacro() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Macro", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { viewModel.cancelMacroRecording() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // New Macro Modal Dialog Box
        if (showNewMacroDialog) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C30)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "START MACRO RECORDING",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB388FF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newRoutineNameInput,
                        onValueChange = { newRoutineNameInput = it },
                        label = { Text("Macro Name", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFF334466),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showNewMacroDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334466))
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showNewMacroDialog = false
                                viewModel.startMacroRecording(newRoutineNameInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Recording")
                        }
                    }
                }
            }
        }

        // Saved Macros Library
        Text(
            text = "ROUTINE LIBRARY (${macros.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(macros, key = { it.id }) { routine ->
                MacroRoutineCard(
                    routine = routine,
                    onReplayClick = {
                        viewModel.replayMacro(routine)
                    },
                    onDeleteClick = {
                        viewModel.deleteMacro(routine.id)
                    }
                )
            }
        }
    }
}

@Composable
fun MacroRoutineCard(
    routine: MacroRoutine,
    onReplayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1526)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF202B48))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = routine.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        if (routine.isPrebuilt) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("BUILT-IN", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                            }
                        }
                    }
                    Text(
                        text = routine.description,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Row {
                    Button(
                        onClick = onReplayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Replay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!routine.isPrebuilt) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Steps Preview Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                routine.steps.take(4).forEach { step ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF192238))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${step.stepNumber}. ${step.title}",
                            fontSize = 9.sp,
                            color = Color(0xFF80D8FF),
                            maxLines = 1
                        )
                    }
                }
                if (routine.steps.size > 4) {
                    Text(
                        text = "+${routine.steps.size - 4} more",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
