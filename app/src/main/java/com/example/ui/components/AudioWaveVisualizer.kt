package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveVisualizer(
    isActive: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    maxHeight: Dp = 36.dp
) {
    val transition = rememberInfiniteTransition(label = "WaveformAnim")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barDelays = listOf(0, 120, 240, 80, 190, 310, 150, 280, 50, 210, 330, 90, 170, 260, 40, 200, 320, 110)

        for (i in 0 until barCount) {
            val delay = barDelays[i % barDelays.size]
            val animatedHeightFraction by transition.animateFloat(
                initialValue = 0.15f,
                targetValue = if (isActive) (0.35f + (0.65f * (Math.sin(i.toDouble()).toFloat().coerceIn(0.2f, 1f)))) else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (delay / 2),
                        delayMillis = delay / 3,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BarHeight_$i"
            )

            val currentHeight = if (isActive) maxHeight * animatedHeightFraction else 4.dp

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(currentHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
