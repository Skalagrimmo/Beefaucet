package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HoneycombSliderCaptcha(
    onSolved: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSolved by remember { mutableStateOf(false) }
    var targetPercent by remember { mutableFloatStateOf(0.72f) }
    val thumbOffsetPx = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Reset when not solved
    LaunchedEffect(Unit) {
        targetPercent = 0.65f + (kotlin.random.Random.nextFloat() * 0.20f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        if (isSolved) HoneyMintTertiary else HoneyGoldPrimary.copy(alpha = 0.5f),
                        CyberAmberSecondary.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSolved) Icons.Default.CheckCircle else Icons.Default.Shield,
                    contentDescription = "Security Status",
                    tint = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSolved) "Verification Completed" else "Honeycomb Slide Verification",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = if (isSolved) "100% Match" else "Target: ${(targetPercent * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSolved) HoneyMintTertiary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isSolved)
                "Verified! Human honey collector authenticated."
            else
                "Drag the golden bee hexagon into the glowing honeycomb socket",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Slider track
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF0F1014))
                .border(1.dp, Color(0xFF2C303D), RoundedCornerShape(32.dp))
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val trackWidthPx = with(density) { maxWidth.toPx() }
            val thumbSizeDp = 56.dp
            val thumbSizePx = with(density) { thumbSizeDp.toPx() }
            val maxOffsetPx = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
            val targetOffsetPx = maxOffsetPx * targetPercent

            // Target socket in honeycomb style
            Box(
                modifier = Modifier
                    .offset { IntOffset(targetOffsetPx.roundToInt(), 0) }
                    .size(thumbSizeDp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSolved) HoneyMintTertiary.copy(alpha = 0.25f)
                        else HoneyGoldPrimary.copy(alpha = 0.15f)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSolved) HoneyMintTertiary else HoneyGoldLight.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⬡",
                    fontSize = 28.sp,
                    color = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary
                )
            }

            // Draggable bee thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetPx.value.roundToInt(), 0) }
                    .size(thumbSizeDp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isSolved) HoneyMintTertiary else HoneyGoldLight,
                                if (isSolved) Color(0xFF00B050) else HoneyGoldPrimary,
                                CyberAmberSecondary
                            )
                        )
                    )
                    .testTag("honeycomb_slider_thumb")
                    .pointerInput(isSolved, maxOffsetPx, targetOffsetPx) {
                        if (isSolved) return@pointerInput
                        detectDragGestures(
                            onDragEnd = {
                                val distance = abs(thumbOffsetPx.value - targetOffsetPx)
                                val tolerancePx = thumbSizePx * 0.25f // comfortable tolerance
                                if (distance <= tolerancePx) {
                                    scope.launch {
                                        thumbOffsetPx.animateTo(targetOffsetPx, spring())
                                        isSolved = true
                                        onSolved()
                                    }
                                } else {
                                    scope.launch {
                                        thumbOffsetPx.animateTo(0f, spring())
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val nextOffset = (thumbOffsetPx.value + dragAmount.x).coerceIn(0f, maxOffsetPx)
                            scope.launch {
                                thumbOffsetPx.snapTo(nextOffset)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSolved) "✓" else "🐝",
                    fontSize = if (isSolved) 22.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSolved) Color.White else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Slide to Hive Socket", fontSize = 11.sp, color = HoneyGoldPrimary, fontWeight = FontWeight.Medium)
            Text("100%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
