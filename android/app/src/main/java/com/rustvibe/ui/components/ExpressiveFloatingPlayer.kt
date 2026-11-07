package com.rustvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import java.io.File

@Composable
fun ExpressiveFloatingPlayer(
    title: String,
    artist: String,
    thumbnailPath: String?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompact = maxWidth < 340.dp
        val isVeryCompact = maxWidth < 260.dp
        val barHeight = if (isCompact) 50.dp else 58.dp
        val thumbSize = if (isCompact) 36.dp else 42.dp
        val playBtnSize = if (isCompact) 36.dp else 42.dp
        val playIconSize = if (isCompact) 18.dp else 24.dp
        val titleSize = if (isCompact) 12.sp else 13.sp
        val artistSize = if (isCompact) 10.sp else 11.sp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(FullPillShape)
                .background(colors.surfaceHigh)
                .clickable(onClick = onPlayerClick)
                .padding(horizontal = if (isCompact) 6.dp else 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Thumbnail
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(colors.accentContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!thumbnailPath.isNullOrEmpty() && File(thumbnailPath).exists() && File(thumbnailPath).length() > 0) {
                        AsyncImage(
                            model = File(thumbnailPath),
                            contentDescription = "Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = ExpressiveIcons.Music,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 12.dp))

                // Title + Artist with Marquee for long text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = artist,
                        fontSize = artistSize,
                        color = colors.textSub,
                        maxLines = 1
                    )
                }

                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(playBtnSize)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) ExpressiveIcons.Pause else ExpressiveIcons.Play,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = PureWhite,
                        modifier = Modifier.size(playIconSize)
                    )
                }

                if (!isVeryCompact) {
                    Spacer(modifier = Modifier.width(6.dp))

                    // Next Track Button
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 30.dp else 36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNextClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.Next,
                            contentDescription = "Next",
                            tint = colors.textSub,
                            modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                        )
                    }
                }
            }
        }
    }
}
