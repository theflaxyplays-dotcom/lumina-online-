package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LuminaPersona

@Composable
fun LuminaHeaderBar(
    currentPersona: LuminaPersona,
    isAccessibilityActive: Boolean,
    isGuardModeActive: Boolean,
    onSwitchPersonaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(currentPersona.primaryColorHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Persona Identity
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(primaryColor, Color(currentPersona.accentColorHex))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentPersona.displayName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LUMINA AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.25f))
                            .border(1.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentPersona.displayName.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor
                        )
                    }
                }
                Text(
                    text = currentPersona.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        // Status Badges & Switch Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Accessibility Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isAccessibilityActive) Color(0xFF00E676).copy(alpha = 0.15f)
                        else Color(0xFFFF5252).copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isAccessibilityActive) Color(0xFF00E676).copy(alpha = 0.5f)
                        else Color(0xFFFF5252).copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isAccessibilityActive) Color(0xFF00E676) else Color(0xFFFF5252))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAccessibilityActive) "UI LIVE" else "NO A11Y",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAccessibilityActive) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                }
            }

            // Persona Switcher Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2333))
                    .border(1.dp, Color(0xFF333B54), CircleShape)
                    .clickable { onSwitchPersonaClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch Persona",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
