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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.ActionCard
import com.example.ui.components.LuminaHeaderBar
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun ChatConsoleScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val persona by viewModel.currentPersona.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val isGuardModeActive by viewModel.isGuardModeActive.collectAsState()

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val primaryColor = Color(persona.primaryColorHex)

    val promptSuggestions = listOf(
        "Turn on torch",
        "Open WhatsApp and send hello",
        "Start Instagram Reels auto-scroll",
        "Record new macro routine",
        "Trigger Emergency SOS sequence",
        "Analyze current screen UI tree",
        "BGMI Co-Caster airdrop commentary"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
    ) {
        // Header
        LuminaHeaderBar(
            currentPersona = persona,
            isAccessibilityActive = isAccessibilityActive,
            isGuardModeActive = isGuardModeActive,
            onSwitchPersonaClick = {
                val next = when (persona) {
                    com.example.data.model.LuminaPersona.LUMINA -> com.example.data.model.LuminaPersona.FRIDAY
                    com.example.data.model.LuminaPersona.FRIDAY -> com.example.data.model.LuminaPersona.VENOM
                    com.example.data.model.LuminaPersona.VENOM -> com.example.data.model.LuminaPersona.LUMINA
                }
                viewModel.switchPersona(next)
            }
        )

        // Clear Chat Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEURAL CONVERSATION LOG",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Chat",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onSpeakClick = { viewModel.speakMessage(message) },
                    onExecuteActionClick = { action -> viewModel.executeAction(action) }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lumina is reasoning and generating action plan...",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Prompt Suggestions Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(promptSuggestions) { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131A2E))
                        .border(1.dp, Color(0xFF263352), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.sendMessage(suggestion)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Input Field Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C101D))
                .border(
                    width = 1.dp,
                    color = Color(0xFF1F2942),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isListening) viewModel.stopVoiceInput() else viewModel.startVoiceInput()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) primaryColor else Color.White.copy(alpha = 0.7f)
                    )
                }

                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            text = "Message ${persona.displayName}...",
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
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            viewModel.sendMessage(inputMessage)
                            inputMessage = ""
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
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onSpeakClick: () -> Unit,
    onExecuteActionClick: (com.example.data.model.LuminaAction) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val personaColor = Color(message.persona.primaryColorHex)

    val cleanText = message.text
        .replace(Regex("```json[\\s\\S]*?```"), "")
        .trim()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isUser) "YOU (Flaxy)" else message.persona.displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser) Color(0xFF80D8FF) else personaColor
            )
        }

        // Bubble Content
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(
                    if (isUser) Brush.linearGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))
                    else Brush.linearGradient(listOf(Color(0xFF14192A), Color(0xFF101524)))
                )
                .border(
                    1.dp,
                    if (isUser) Color(0xFF42A5F5) else personaColor.copy(alpha = 0.3f),
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = cleanText,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "TTS",
                                tint = personaColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Parsed Action Card (if any)
        if (message.parsedAction != null) {
            Spacer(modifier = Modifier.height(6.dp))
            ActionCard(
                action = message.parsedAction,
                onExecuteClick = onExecuteActionClick,
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }
    }
}
