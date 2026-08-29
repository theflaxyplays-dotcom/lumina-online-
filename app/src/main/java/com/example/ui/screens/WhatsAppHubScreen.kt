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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoReplyStatus
import com.example.data.model.LuminaStatusMode
import com.example.data.model.WhatsAppChatThread
import com.example.service.LuminaNotificationListener
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun WhatsAppHubScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val threads by viewModel.whatsAppThreads.collectAsState()
    val repliesLog by viewModel.whatsAppReplies.collectAsState()
    val currentMode by viewModel.statusMode.collectAsState()
    val isListenerActive by LuminaNotificationListener.isListenerConnected.collectAsState()

    var selectedFilter by remember { mutableStateOf<com.example.data.model.AppSource?>(null) }
    var testSenderName by remember { mutableStateOf("Client Project Alpha") }
    var testIncomingText by remember { mutableStateOf("Hey Flaxy, need update on app build.") }

    val filteredThreads = if (selectedFilter == null) threads else threads.filter { it.appSource == selectedFilter }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "WHATSAPP DIRECTREPLY HUB",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color(0xFF25D366)
        )
        Text(
            text = "Background Auto-Reply Agent using Notification RemoteInput",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Mode Switcher Bar
        Text(
            text = "ACTIVE STATUS PROFILE (CONTROLS AUTO-REPLY LOGIC)",
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
            LuminaStatusMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF25D366).copy(alpha = 0.25f) else Color(0xFF131A2A))
                        .border(1.dp, if (isSelected) Color(0xFF25D366) else Color(0xFF232E48), RoundedCornerShape(8.dp))
                        .clickable { viewModel.setStatusMode(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF25D366) else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Current Mode Message Template
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A22)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F3D32))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ACTIVE AUTO-REPLY TEMPLATE:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF69F0AE)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${currentMode.autoReplyMessage}\"",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Filter Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(null to "All Apps") + com.example.data.model.AppSource.values().map { it to it.displayName }
            filters.forEach { (source, label) ->
                val isSelected = selectedFilter == source
                val tintColor = source?.let { Color(it.colorHex) } ?: Color(0xFF00E5FF)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) tintColor.copy(alpha = 0.2f) else Color(0xFF101524))
                        .border(1.dp, if (isSelected) tintColor else Color(0xFF222C44), RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = source }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) tintColor else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Intercepted Chat Threads Feed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INTERCEPTED NOTIFICATIONS (${filteredThreads.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Text(
                text = if (isListenerActive) "LISTENER ONLINE" else "LISTENER STANDBY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isListenerActive) Color(0xFF00E676) else Color(0xFFFFB300)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredThreads, key = { it.id }) { thread ->
                WhatsAppThreadCard(
                    thread = thread,
                    onAutoReplyClick = {
                        viewModel.triggerWhatsAppReplySimulated(thread, currentMode.autoReplyMessage)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulation Test Dispatcher
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263352))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "TEST SIMULATED INCOMING WHATSAPP MESSAGE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = testSenderName,
                        onValueChange = { testSenderName = it },
                        label = { Text("Sender", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(0.4f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = testIncomingText,
                        onValueChange = { testIncomingText = it },
                        label = { Text("Message", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        val simThread = WhatsAppChatThread(
                            id = "wa_${System.currentTimeMillis()}",
                            senderName = testSenderName,
                            lastMessage = testIncomingText,
                            timeAgo = "Just now",
                            status = AutoReplyStatus.PENDING
                        )
                        viewModel.triggerWhatsAppReplySimulated(simThread, currentMode.autoReplyMessage)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Intercept & Send DirectReply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WhatsAppThreadCard(
    thread: WhatsAppChatThread,
    onAutoReplyClick: () -> Unit
) {
    val appSourceColor = Color(thread.appSource.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1420)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E283C))
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(appSourceColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = thread.senderName.take(1),
                            color = appSourceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = thread.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(appSourceColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = thread.appSource.displayName,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = appSourceColor
                                )
                            }
                        }
                        Text(
                            text = thread.timeAgo,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                if (thread.status == AutoReplyStatus.AUTO_REPLIED) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("AUTO-REPLIED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                    }
                } else {
                    Button(
                        onClick = onAutoReplyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = appSourceColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Auto Reply", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "📩 \"${thread.lastMessage}\"",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            if (thread.autoRepliedText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF13201B))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "🤖 Lumina Sent: \"${thread.autoRepliedText}\"",
                        fontSize = 11.sp,
                        color = Color(0xFF69F0AE)
                    )
                }
            }
        }
    }
}
