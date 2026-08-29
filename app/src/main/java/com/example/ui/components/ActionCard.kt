package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LuminaAction

@Composable
fun ActionCard(
    action: LuminaAction,
    onExecuteClick: (LuminaAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (action.type.uppercase()) {
        "SOS_TRIGGER" -> Color(0xFFFF1744)
        "SYSTEM_SETTING" -> Color(0xFFFF9100)
        "ACCESSIBILITY_NODE" -> Color(0xFF00E5FF)
        "GESTURE_SWIPE", "GESTURE_TAP" -> Color(0xFF76FF03)
        "INTENT" -> Color(0xFF25D366)
        else -> Color(0xFFD500F9)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1424))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ACTION: ${action.type}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                    if (action.target_package != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = action.target_package,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Details
            if (action.node_query != null) {
                Text(
                    text = "Node Query: view_id='${action.node_query.view_id ?: ""}' text='${action.node_query.text ?: ""}'",
                    fontSize = 11.sp,
                    color = Color(0xFF80D8FF),
                    fontFamily = FontFamily.Monospace
                )
            }

            if (action.payload?.input_text != null) {
                Text(
                    text = "Input Payload: \"${action.payload.input_text}\"",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (action.payload?.setting_key != null) {
                Text(
                    text = "Setting Key: ${action.payload.setting_key} -> ${action.payload.setting_value ?: "toggle"}",
                    fontSize = 11.sp,
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Execute Button
            Button(
                onClick = { onExecuteClick(action) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Execute",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EXECUTE AUTONOMOUS ACTION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
