package com.example.tutora.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutora.ui.theme.VibrantBlue
import com.example.tutora.ui.theme.VibrantTeal

@Composable
fun TutoraLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpinnerTransition")

    // Rotation animation for the outer arc
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationOuter"
    )

    // Rotation animation for the inner arc (reverse)
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationInner"
    )

    // Pulse animation for the central logo
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            
            // Outer Arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(VibrantBlue, VibrantTeal.copy(alpha = 0.1f), VibrantBlue)
                ),
                startAngle = rotationOuter,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.toPx(), size.toPx())
            )

            // Middle Arc (static or subtle)
            drawArc(
                color = VibrantBlue.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = Size(size.toPx(), size.toPx())
            )

            // Inner Arc
            val innerSize = size.toPx() * 0.7f
            val offset = (size.toPx() - innerSize) / 2
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(VibrantTeal, VibrantBlue.copy(alpha = 0.1f), VibrantTeal)
                ),
                startAngle = rotationInner,
                sweepAngle = 200f,
                useCenter = false,
                style = Stroke(width = strokeWidth / 1.5f, cap = StrokeCap.Round),
                size = Size(innerSize, innerSize),
                topLeft = Offset(offset, offset)
            )
        }

        // Central Pulsing Logo Text
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "T",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}
