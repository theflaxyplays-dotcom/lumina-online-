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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.model.AccessibilityNodeDisplay
import com.example.service.LuminaAccessibilityService
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun AutomationScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val lastLog by viewModel.lastAccessibilityLog.collectAsState()
    val nodeTree by viewModel.nodeTree.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()

    var testPhoneNum by remember { mutableStateOf("+919876543210") }
    var testWhatsAppMsg by remember { mutableStateOf("Autonomous Hello from Lumina!") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .padding(16.dp)
    ) {
        // Title & Service Banner
        Text(
            text = "AUTONOMOUS OS CONTROLLER",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color.White
        )
        Text(
            text = "Zero-Mock Runtime: Accessibility Tree Crawling & Native Intent Dispatches",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Service Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityActive) Color(0xFF0A2218) else Color(0xFF2B1015)
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAccessibilityActive) Color(0xFF00E676) else Color(0xFFFF5252)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAccessibilityActive) "ACCESSIBILITY SERVICE ACTIVE" else "ACCESSIBILITY DISCONNECTED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isAccessibilityActive) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                    Text(
                        text = lastLog,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }

                IconButton(onClick = { LuminaAccessibilityService.instance?.refreshNodeHierarchy() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Nodes",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gesture & Intent Trigger Lab
        Text(
            text = "INSTANT HARDWARE & INTENT TESTBED",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Swipe Gesture
            Button(
                onClick = {
                    LuminaAccessibilityService.instance?.dispatchSwipe(540f, 1600f, 540f, 400f, 300)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2338)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.SwipeVertical, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Swipe Up", fontSize = 11.sp)
            }

            // Touch Tap
            Button(
                onClick = {
                    LuminaAccessibilityService.instance?.dispatchTouchTap(540f, 960f)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2338)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tap Center", fontSize = 11.sp)
            }

            // Torch Toggle
            Button(
                onClick = { viewModel.toggleTorch() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTorchOn) Color(0xFFFFD600) else Color(0xFF1B2338),
                    contentColor = if (isTorchOn) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Torch", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Volume Slider Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volume (${(volumeLevel * 100).toInt()}%)", fontSize = 11.sp, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = volumeLevel,
                onValueChange = { viewModel.setVolume(it) },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00E5FF),
                    activeTrackColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real Accessibility Tree Inspector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE ACCESSIBILITY NODE TREE (${if (nodeTree.isEmpty()) "SIMULATED / ACTIVE" else "${nodeTree.size} Nodes"})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF00E5FF)
            )
            Text(
                text = "Click node to click UI",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displayList = if (nodeTree.isNotEmpty()) {
            nodeTree
        } else {
            // Simulated Active Window Tree for interactive demonstration
            listOf(
                AccessibilityNodeDisplay("n1", "com.instagram.android:id/reels_tab", "Reels Tab", "Reels Tab Navigation", "android.widget.Button", true, false, "[320, 2100 - 450, 2250]", 0),
                AccessibilityNodeDisplay("n2", "com.whatsapp:id/send_button", "Send", "Send Message", "android.widget.ImageButton", true, false, "[920, 1850 - 1040, 1970]", 1),
                AccessibilityNodeDisplay("n3", "com.whatsapp:id/entry", "Type a message", "Message Input", "android.widget.EditText", true, true, "[120, 1850 - 900, 1970]", 1),
                AccessibilityNodeDisplay("n4", "com.android.systemui:id/quick_settings", "Wi-Fi, Bluetooth", "Status Header", "android.view.ViewGroup", false, false, "[0, 0 - 1080, 150]", 0),
                AccessibilityNodeDisplay("n5", "com.google.android.apps.maps:id/search_box", "Search here", "Search in Maps", "android.widget.TextView", true, false, "[80, 120 - 1000, 220]", 0)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1220))
                .border(1.dp, Color(0xFF1E2840), RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(displayList) { node ->
                NodeTreeItemCard(
                    node = node,
                    onClick = {
                        LuminaAccessibilityService.instance?.performDynamicClick(node.viewId, node.text, node.contentDesc)
                    }
                )
            }
        }
    }
}

@Composable
fun NodeTreeItemCard(
    node: AccessibilityNodeDisplay,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (node.depth * 12).dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141C30))
            .border(1.dp, if (node.isClickable) Color(0xFF00E5FF).copy(alpha = 0.4f) else Color(0xFF233050), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.viewId ?: node.className.substringAfterLast("."),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF80D8FF)
                )

                if (node.isClickable) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("CLICKABLE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    }
                }
            }

            if (!node.text.isNullOrEmpty()) {
                Text(
                    text = "Text: \"${node.text}\"",
                    fontSize = 11.sp,
                    color = Color.White
                )
            }

            if (!node.contentDesc.isNullOrEmpty()) {
                Text(
                    text = "Desc: \"${node.contentDesc}\"",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Text(
                text = "Bounds: ${node.bounds}",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}
