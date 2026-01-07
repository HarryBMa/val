package com.yourcompany.val.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Beautiful audio visualizer for AI speech - inspired by ChatGPT voice mode
 * Features smooth animated circular waves with ocean-inspired colors
 */
@Composable
fun AudioVisualizer(
    isAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isAnimating) {
            // Multiple wave layers for depth
            AnimatedWaveCircle(
                delay = 0,
                baseRadius = 80f,
                amplitude = 20f,
                frequency = 8,
                color = Color(0xFF7c9cb5),
                strokeWidth = 3f
            )

            AnimatedWaveCircle(
                delay = 200,
                baseRadius = 110f,
                amplitude = 15f,
                frequency = 10,
                color = Color(0xFFa8c5d9),
                strokeWidth = 2.5f
            )

            AnimatedWaveCircle(
                delay = 400,
                baseRadius = 140f,
                amplitude = 12f,
                frequency = 12,
                color = Color(0xFFb8d8d8),
                strokeWidth = 2f
            )

            AnimatedWaveCircle(
                delay = 600,
                baseRadius = 170f,
                amplitude = 10f,
                frequency = 14,
                color = Color(0xFFd4e8ed),
                strokeWidth = 1.5f
            )

            // Central pulsing gradient circle
            PulsingCenterCircle()
        }
    }
}

/**
 * Animated wave circle that oscillates smoothly
 */
@Composable
private fun AnimatedWaveCircle(
    delay: Int,
    baseRadius: Float,
    amplitude: Float,
    frequency: Int,
    color: Color,
    strokeWidth: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave-$delay")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000 + delay,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase-$delay"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500 + delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale-$delay"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800 + delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha-$delay"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val adjustedRadius = baseRadius * scale

        val points = mutableListOf<Offset>()
        val steps = 100

        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2f * PI.toFloat()
            val wave = amplitude * sin(frequency * angle + phase)
            val radius = adjustedRadius + wave

            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            points.add(Offset(x, y))
        }

        // Draw smooth wave path
        for (i in 0 until points.size - 1) {
            drawLine(
                color = color.copy(alpha = alpha),
                start = points[i],
                end = points[i + 1],
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Pulsing center circle with gradient
 */
@Composable
private fun PulsingCenterCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "center-pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "center-scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "center-alpha"
    )

    Canvas(
        modifier = Modifier
            .size(120.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = 50f * scale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF7c9cb5).copy(alpha = alpha),
                    Color(0xFF5d7a8f).copy(alpha = alpha * 0.6f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // Inner solid circle
        drawCircle(
            color = Color(0xFF7c9cb5).copy(alpha = alpha),
            radius = radius * 0.3f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Simpler bar-style visualizer (alternative option)
 */
@Composable
fun BarAudioVisualizer(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isAnimating) {
            Row(
                modifier = Modifier.fillMaxWidth(0.6f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(barCount) { index ->
                    AnimatedBar(
                        delay = index * 100,
                        color = Color(0xFF7c9cb5)
                    )
                }
            }
        }
    }
}

/**
 * Individual animated bar for bar visualizer
 */
@Composable
private fun AnimatedBar(
    delay: Int,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bar-$delay")

    val height by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600 + delay,
                easing = FastOutSlowInEasing,
                delayMillis = delay
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar-height-$delay"
    )

    Canvas(
        modifier = Modifier
            .width(8.dp)
            .height(60.dp)
    ) {
        val barHeight = size.height * height
        val barWidth = size.width

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    color,
                    color.copy(alpha = 0.3f)
                )
            ),
            topLeft = Offset(0f, (size.height - barHeight) / 2),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
        )
    }
}
