package com.rustvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.GridCoverShape
import java.io.File

@Composable
fun PlaylistCoverCard(
    customCoverPath: String?,
    thumbnails: List<String>,
    size: Dp = 60.dp,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .size(size)
            .clip(GridCoverShape)
            .background(colors.surface),
        contentAlignment = Alignment.Center
    ) {
        if (!customCoverPath.isNullOrEmpty() && File(customCoverPath).exists()) {
            // Option A: Custom User Image
            AsyncImage(
                model = File(customCoverPath),
                contentDescription = "Cover Playlist",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (thumbnails.size >= 4) {
            // Option B: Dynamic 2x2 Grid Mosaic (4 Quadrants)
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    MosaicQuadrant(thumbnails[0], Modifier.weight(1f))
                    MosaicQuadrant(thumbnails[1], Modifier.weight(1f))
                }
                Row(modifier = Modifier.weight(1f)) {
                    MosaicQuadrant(thumbnails[2], Modifier.weight(1f))
                    MosaicQuadrant(thumbnails[3], Modifier.weight(1f))
                }
            }
        } else if (thumbnails.isNotEmpty()) {
            // Single Thumbnail fallback
            AsyncImage(
                model = File(thumbnails.first()),
                contentDescription = "Cover Track",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Empty placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ExpressiveIcons.Music,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
        }
    }
}

@Composable
private fun MosaicQuadrant(imagePath: String, modifier: Modifier) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        if (File(imagePath).exists()) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.accentContainer)
            )
        }
    }
}
