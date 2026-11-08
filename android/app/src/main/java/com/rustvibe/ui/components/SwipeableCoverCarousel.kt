package com.rustvibe.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rustvibe.data.local.TrackEntity
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

@Composable
fun SwipeableCoverCarousel(
    currentTrack: TrackEntity?,
    queue: List<TrackEntity>,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    coverSize: Dp = 250.dp,
    cornerRadius: Dp = 26.dp,
    showHintAndDots: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    var accumulatedDrag by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    // Reset drag offset when track changes
    LaunchedEffect(currentTrack?.id) {
        accumulatedDrag = 0f
        dragOffset.snapTo(0f)
    }

    val currentIndex = if (currentTrack != null && queue.isNotEmpty()) {
        queue.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
    } else {
        0
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Cover Box with Horizontal Drag Detector
        Box(
            modifier = Modifier
                .size(coverSize)
                .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                .graphicsLayer {
                    // Subtle tilt rotation during swipe
                    rotationZ = (dragOffset.value / 140f) * 4f
                }
                .clip(RoundedCornerShape(cornerRadius))
                .background(colors.surface)
                .pointerInput(currentTrack?.id) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            accumulatedDrag = 0f
                        },
                        onDragEnd = {
                            val threshold = 30f
                            val value = accumulatedDrag
                            if (value < -threshold) {
                                onSkipNext()
                            } else if (value > threshold) {
                                onSkipPrev()
                            }
                            accumulatedDrag = 0f
                            coroutineScope.launch {
                                dragOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            accumulatedDrag = 0f
                            coroutineScope.launch {
                                dragOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            accumulatedDrag += dragAmount
                            val damped = (accumulatedDrag * 0.75f).coerceIn(-130f, 130f)
                            coroutineScope.launch {
                                dragOffset.snapTo(damped)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val thumbPath = currentTrack?.thumbnailPath
            if (!thumbPath.isNullOrEmpty() && File(thumbPath).exists() && File(thumbPath).length() > 0) {
                AsyncImage(
                    model = File(thumbPath),
                    contentDescription = "Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = ExpressiveIcons.Music,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(coverSize * 0.35f)
                )
            }
        }

        if (showHintAndDots) {
            Spacer(modifier = Modifier.height(10.dp))

            // Swipe Direction Indicator Pill (FullPillShape 9999px)
            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ExpressiveIcons.Prev,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Geser Cover Lagu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSub
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = ExpressiveIcons.Next,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Queue Pagination Dots (Morphing Pill Dots)
            if (queue.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxDots = 5
                    val displayCount = queue.size.coerceAtMost(maxDots)
                    val activeDotIndex = if (queue.size <= maxDots) {
                        currentIndex
                    } else {
                        ((currentIndex.toFloat() / queue.size.toFloat()) * maxDots).toInt().coerceIn(0, maxDots - 1)
                    }

                    for (i in 0 until displayCount) {
                        val isActive = (i == activeDotIndex)
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (isActive) 18.dp else 6.dp,
                                    height = 6.dp
                                )
                                .clip(if (isActive) FullPillShape else CircleShape)
                                .background(if (isActive) colors.primary else colors.surfaceHighest)
                        )
                    }
                }
            }
        }
    }
}
