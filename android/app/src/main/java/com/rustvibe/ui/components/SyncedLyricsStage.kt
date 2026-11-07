package com.rustvibe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.audio.LyricLine
import com.rustvibe.audio.LyricsParser
import com.rustvibe.ui.theme.AppTheme

@Composable
fun SyncedLyricsStage(
    lyrics: List<LyricLine>,
    currentPlaybackMs: Long,
    onSeekRequested: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeIndex = LyricsParser.findActiveLineIndex(lyrics, currentPlaybackMs)
    val colors = AppTheme.colors

    // Smooth auto-scroll to current singing line (M3 Expressive Motion)
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyrics.size) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -120 // Center the line vertically in the container
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Lirik",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSub,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (lyrics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lirik tidak tersedia untuk lagu ini",
                        fontSize = 13.sp,
                        color = colors.textSub.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = index == activeIndex

                        val containerColor by animateColorAsState(
                            targetValue = if (isActive) colors.lyricBg else colors.surface,
                            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                            label = "lyric_bg"
                        )

                        val textColor by animateColorAsState(
                            targetValue = if (isActive) colors.lyricText else if (index < activeIndex) colors.textSub.copy(alpha = 0.35f) else colors.textSub.copy(alpha = 0.65f),
                            animationSpec = tween(durationMillis = 250),
                            label = "lyric_color"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(containerColor)
                                .clickable { onSeekRequested(line.timeMs) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = line.text,
                                fontSize = if (isActive) 16.sp else 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}
