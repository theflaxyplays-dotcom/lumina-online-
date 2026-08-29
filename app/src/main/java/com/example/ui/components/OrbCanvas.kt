package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.OrbTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LuminaOrb(
    theme: OrbTheme,
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isThinking -> 1800
                    isSpeaking -> 3000
                    isListening -> 4000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Reverse Rotation angle for counter-orbit
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isThinking -> 2200
                    isSpeaking -> 3500
                    else -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterRotation"
    )

    // Breathing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = if (isListening || isSpeaking) 1.14f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 650 else if (isSpeaking) 800 else 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val primaryColor = Color(theme.primaryColor)
    val secondaryColor = Color(theme.secondaryColor)
    val ringColor = Color(theme.ringColor)

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val baseRadius = (this.size.minDimension / 2) * 0.58f * pulseScale

            // 1. Outer Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (isListening || isSpeaking) 0.35f else 0.18f),
                        secondaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.6f
                ),
                radius = baseRadius * 1.6f,
                center = center
            )

            // 2. Orbital Reticle Ring 1
            drawOrbitalRing(
                center = center,
                radius = baseRadius * 1.25f,
                angle = rotationAngle,
                color = ringColor.copy(alpha = 0.7f),
                strokeWidth = 2.5f,
                dashes = true
            )

            // 3. Orbital Reticle Ring 2 (Tilted counter ring)
            drawOrbitalRing(
                center = center,
                radius = baseRadius * 1.4f,
                angle = counterRotation,
                color = secondaryColor.copy(alpha = 0.6f),
                strokeWidth = 2.0f,
                dashes = false
            )

            // 4. Inner Glowing Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor.copy(alpha = 0.9f),
                        secondaryColor.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 5. Floating Energy Nodes
            val numNodes = if (isThinking) 8 else 6
            for (i in 0 until numNodes) {
                val nodeAngle = Math.toRadians((rotationAngle + (i * (360f / numNodes))).toDouble())
                val distance = baseRadius * (0.95f + 0.25f * sin(nodeAngle * 2).toFloat())
                val nodeX = center.x + (distance * cos(nodeAngle)).toFloat()
                val nodeY = center.y + (distance * sin(nodeAngle)).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = if (isSpeaking || isListening) 5f else 3.5f,
                    center = Offset(nodeX, nodeY)
                )

                // Connecting line to center
                drawLine(
                    color = primaryColor.copy(alpha = 0.25f),
                    start = center,
                    end = Offset(nodeX, nodeY),
                    strokeWidth = 1.2f
                )
            }
        }
    }
}

private fun DrawScope.drawOrbitalRing(
    center: Offset,
    radius: Float,
    angle: Float,
    color: Color,
    strokeWidth: Float,
    dashes: Boolean
) {
    val sweep = if (dashes) 100f else 260f
    drawArc(
        color = color,
        startAngle = angle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    if (dashes) {
        drawArc(
            color = color.copy(alpha = 0.5f),
            startAngle = angle + 180f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
