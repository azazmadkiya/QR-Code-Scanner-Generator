package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val scale = remember { Animatable(0.82f) }
    val alpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    // Infinite laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserSweep"
    )

    // Pulse animation for outer glow
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
            )
        }
        delay(2000L)
        onTimeout()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor,
                        surfaceColor,
                        primaryContainer.copy(alpha = 0.35f)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTimeout
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Glowing App Icon Frame
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(glowScale)
            ) {
                // Background soft shadow glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Card container
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(136.dp)
                ) {
                    // Custom Canvas drawing QR finder matrix and animated laser beam
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val pad = 24.dp.toPx()
                        val matrixSize = canvasWidth - (pad * 2)

                        // Finder pattern dimensions
                        val finderSize = matrixSize * 0.28f
                        val dotSize = matrixSize * 0.08f

                        // Top-left finder
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(pad, pad),
                            size = Size(finderSize, finderSize),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(pad + finderSize * 0.25f, pad + finderSize * 0.25f),
                            size = Size(finderSize * 0.5f, finderSize * 0.5f),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        // Top-right finder
                        val trX = pad + matrixSize - finderSize
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(trX, pad),
                            size = Size(finderSize, finderSize),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(trX + finderSize * 0.25f, pad + finderSize * 0.25f),
                            size = Size(finderSize * 0.5f, finderSize * 0.5f),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        // Bottom-left finder
                        val blY = pad + matrixSize - finderSize
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(pad, blY),
                            size = Size(finderSize, finderSize),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(pad + finderSize * 0.25f, blY + finderSize * 0.25f),
                            size = Size(finderSize * 0.5f, finderSize * 0.5f),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        // Data matrix dots
                        val dotPositions = listOf(
                            Offset(pad + matrixSize * 0.45f, pad + matrixSize * 0.15f),
                            Offset(pad + matrixSize * 0.58f, pad + matrixSize * 0.28f),
                            Offset(pad + matrixSize * 0.42f, pad + matrixSize * 0.42f),
                            Offset(pad + matrixSize * 0.58f, pad + matrixSize * 0.54f),
                            Offset(pad + matrixSize * 0.75f, pad + matrixSize * 0.45f),
                            Offset(pad + matrixSize * 0.85f, pad + matrixSize * 0.62f),
                            Offset(pad + matrixSize * 0.45f, pad + matrixSize * 0.72f),
                            Offset(pad + matrixSize * 0.62f, pad + matrixSize * 0.85f),
                            Offset(pad + matrixSize * 0.78f, pad + matrixSize * 0.78f)
                        )
                        for (pos in dotPositions) {
                            drawRoundRect(
                                color = onSurfaceVariantColor.copy(alpha = 0.65f),
                                topLeft = pos,
                                size = Size(dotSize, dotSize),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }

                        // Corner Viewfinder Brackets
                        val bracketLen = 14.dp.toPx()
                        val bracketStroke = 3.dp.toPx()
                        val bOffset = 10.dp.toPx()

                        // TL
                        drawLine(tertiaryColor, Offset(bOffset, bOffset), Offset(bOffset + bracketLen, bOffset), bracketStroke, StrokeCap.Round)
                        drawLine(tertiaryColor, Offset(bOffset, bOffset), Offset(bOffset, bOffset + bracketLen), bracketStroke, StrokeCap.Round)
                        // TR
                        drawLine(tertiaryColor, Offset(canvasWidth - bOffset, bOffset), Offset(canvasWidth - bOffset - bracketLen, bOffset), bracketStroke, StrokeCap.Round)
                        drawLine(tertiaryColor, Offset(canvasWidth - bOffset, bOffset), Offset(canvasWidth - bOffset, bOffset + bracketLen), bracketStroke, StrokeCap.Round)
                        // BL
                        drawLine(tertiaryColor, Offset(bOffset, canvasHeight - bOffset), Offset(bOffset + bracketLen, canvasHeight - bOffset), bracketStroke, StrokeCap.Round)
                        drawLine(tertiaryColor, Offset(bOffset, canvasHeight - bOffset), Offset(bOffset, canvasHeight - bOffset - bracketLen), bracketStroke, StrokeCap.Round)
                        // BR
                        drawLine(tertiaryColor, Offset(canvasWidth - bOffset, canvasHeight - bOffset), Offset(canvasWidth - bOffset - bracketLen, canvasHeight - bOffset), bracketStroke, StrokeCap.Round)
                        drawLine(tertiaryColor, Offset(canvasWidth - bOffset, canvasHeight - bOffset), Offset(canvasWidth - bOffset, canvasHeight - bOffset - bracketLen), bracketStroke, StrokeCap.Round)

                        // Animated Laser Scan Beam
                        val laserY = pad + (matrixSize * laserYRatio)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(tertiaryColor.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(canvasWidth / 2f, laserY),
                                radius = 24.dp.toPx()
                            ),
                            radius = 24.dp.toPx(),
                            center = Offset(canvasWidth / 2f, laserY)
                        )
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    tertiaryColor,
                                    tertiaryColor,
                                    Color.Transparent
                                )
                            ),
                            start = Offset(pad - 4.dp.toPx(), laserY),
                            end = Offset(canvasWidth - pad + 4.dp.toPx(), laserY),
                            strokeWidth = 2.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name
            Text(
                text = "QR Master",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = onSurfaceColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = "Scan • Generate • Secure",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = onSurfaceVariantColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Highlight feature badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SplashFeaturePill(icon = Icons.Default.QrCodeScanner, label = "Instant Scan")
                SplashFeaturePill(icon = Icons.Default.OfflinePin, label = "100% Offline")
                SplashFeaturePill(icon = Icons.Default.Lock, label = "AES-256")
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .clip(CircleShape)
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = primaryColor,
                    trackColor = primaryContainer.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tap to skip",
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariantColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SplashFeaturePill(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
