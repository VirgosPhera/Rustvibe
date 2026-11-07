package com.rustvibe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rustvibe.audio.EngineState
import com.rustvibe.data.local.TrackEntity
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.ExpressiveBottomNavigation
import com.rustvibe.ui.components.ExpressiveFloatingPlayer
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.NavTab
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import com.rustvibe.ui.theme.TrackCardShape
import java.io.File

@Composable
fun OfflineScreen(
    viewModel: RustVibeViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylistHub: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val offlineTracks by viewModel.offlineTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val colors = AppTheme.colors

    // Calculate total disk usage
    val totalBytes = offlineTracks.sumOf { track ->
        val m4a = File(track.m4aFilePath).length()
        val mp3 = track.backupMp3FilePath?.let { File(it).length() } ?: 0L
        m4a + mp3
    }
    val totalMb = (totalBytes / (1024 * 1024)).toInt().coerceAtLeast(1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Header
            Text(
                text = "Pustaka Offline",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textMain
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh)
                        .clickable(onClick = onNavigateToHome)
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(text = "Semua", fontSize = 12.sp, color = colors.textSub)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh)
                        .clickable(onClick = onNavigateToPlaylistHub)
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(text = "Playlist", fontSize = 12.sp, color = colors.textSub)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .background(colors.primary)
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(text = "Offline", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Summary Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(TrackCardShape)
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Penyimpanan Offline: $totalMb MB",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                    Text(
                        text = "${offlineTracks.size} Lagu M4A Siap Diputar Tanpa Kuota",
                        fontSize = 11.sp,
                        color = colors.textSub
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Flat Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(FullPillShape)
                            .background(colors.surfaceHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(6.dp)
                                .clip(FullPillShape)
                                .background(colors.primary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Semua Lagu Tersimpan (${offlineTracks.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(offlineTracks) { track ->
                    OfflineTrackRow(
                        track = track,
                        onPlayClick = { viewModel.playTrack(track, offlineTracks) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (currentTrack != null) 146.dp else 84.dp))
        }

        // Floating Mini Player & Bottom Navigation Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (currentTrack != null) {
                ExpressiveFloatingPlayer(
                    title = currentTrack!!.title,
                    artist = currentTrack!!.producer,
                    thumbnailPath = currentTrack!!.thumbnailPath,
                    isPlaying = playbackState == EngineState.PLAYING,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.skipToNext() },
                    onPlayerClick = onNavigateToPlayer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            ExpressiveBottomNavigation(
                selectedTab = NavTab.BERANDA,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.BERANDA -> onNavigateToHome()
                        NavTab.CARI -> onNavigateToSearch()
                        NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                        NavTab.PROFIL -> onNavigateToProfile()
                    }
                }
            )
        }
    }
}

@Composable
private fun OfflineTrackRow(track: TrackEntity, onPlayClick: () -> Unit) {
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
                    text = "${track.producer} • M4A",
                    fontSize = 11.sp,
                    color = colors.textSub
                )
            }

            Icon(
                imageVector = ExpressiveIcons.Check,
                contentDescription = "Tersimpan",
                tint = colors.primary,
                modifier = Modifier
                    .size(22.dp)
                    .padding(end = 4.dp)
            )
        }
    }
}
