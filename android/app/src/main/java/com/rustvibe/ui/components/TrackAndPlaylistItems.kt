package com.rustvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.data.local.TrackEntity
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.TrackCardShape
import java.io.File

@Composable
fun HomePlaylistItem(
    playlist: PlaylistEntity,
    thumbnails: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .size(width = 156.dp, height = 140.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            PlaylistCoverCard(
                customCoverPath = playlist.customCoverPath,
                thumbnails = thumbnails,
                size = 56.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = playlist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain,
                maxLines = 1
            )
            Text(
                text = "Playlist Kustom",
                fontSize = 11.sp,
                color = colors.textSub
            )
        }
    }
}

@Composable
fun HomeTrackItem(track: TrackEntity, onPlayClick: () -> Unit) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .clickable(onClick = onPlayClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!track.thumbnailPath.isNullOrEmpty() && File(track.thumbnailPath).exists() && File(track.thumbnailPath).length() > 0) {
                    AsyncImage(
                        model = File(track.thumbnailPath),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = ExpressiveIcons.Music,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    maxLines = 1
                )
                Text(
                    text = "${track.producer} • ${track.durationSeconds / 60}:${String.format("%02d", track.durationSeconds % 60)}",
                    fontSize = 11.sp,
                    color = colors.textSub
                )
            }

            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "M4A", fontSize = 11.sp, color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
