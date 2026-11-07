package com.rustvibe.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rustvibe.audio.EngineState
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.data.local.TrackEntity
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.ExpressiveBottomNavigation
import com.rustvibe.ui.components.ExpressiveFloatingPlayer
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.ExpressiveNavigationRail
import com.rustvibe.ui.components.NavTab
import com.rustvibe.ui.components.PlaylistCoverCard
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import com.rustvibe.ui.theme.TrackCardShape
import java.io.File

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    tracks: List<TrackEntity>,
    viewModel: RustVibeViewModel,
    onBackClick: () -> Unit,
    onAddTrackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val colors = AppTheme.colors

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // =====================================================================
        // LANDSCAPE MODE (Approved Dual-Pane Layout)
        // =====================================================================
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            ExpressiveNavigationRail(
                selectedTab = NavTab.PLAYLIST,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.BERANDA -> onNavigateToHome()
                        NavTab.CARI -> onNavigateToSearch()
                        NavTab.PROFIL -> onNavigateToProfile()
                        NavTab.PLAYLIST -> {}
                    }
                },
                onToggleTheme = { viewModel.toggleTheme() },
                isDark = colors.isDark
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (currentTrack != null) 68.dp else 12.dp, top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: Playlist Artwork, Title, Count, Add Track
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceHigh)
                                    .clickable(onClick = onBackClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ExpressiveIcons.Back,
                                    contentDescription = "Kembali",
                                    tint = colors.textMain,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Detail Playlist", fontSize = 14.sp, color = colors.textSub)
                        }

                        // Artwork & Metadata
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            PlaylistCoverCard(
                                customCoverPath = playlist.customCoverPath,
                                thumbnails = tracks.map { it.thumbnailPath },
                                size = 88.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = playlist.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain,
                                maxLines = 1
                            )
                            Text(
                                text = "${tracks.size} Lagu • Playlist Kustom",
                                fontSize = 12.sp,
                                color = colors.textSub
                            )
                        }

                        // Add Track Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(FullPillShape)
                                .background(colors.primary)
                                .clickable(onClick = onAddTrackClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = ExpressiveIcons.Plus,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Tambah Lagu", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }

                    // Right Pane: Reorderable Tracklist
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "Daftar Lagu (Geser ⠿ untuk atur urutan)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSub
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(tracks) { index, track ->
                                DetailTrackRow(
                                    position = index + 1,
                                    track = track,
                                    canMoveUp = index > 0,
                                    canMoveDown = index < tracks.size - 1,
                                    onMoveUp = { viewModel.movePlaylistTrack(playlist.id, index, index - 1, tracks) },
                                    onMoveDown = { viewModel.movePlaylistTrack(playlist.id, index, index + 1, tracks) },
                                    onPlayClick = { viewModel.playTrack(track, tracks) }
                                )
                            }
                        }
                    }
                }

                // Wide Floating Mini Player at Bottom
                val activeTrack = currentTrack
                if (activeTrack != null) {
                    ExpressiveFloatingPlayer(
                        title = activeTrack.title,
                        artist = activeTrack.producer,
                        thumbnailPath = activeTrack.thumbnailPath,
                        isPlaying = playbackState == EngineState.PLAYING,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.skipToNext() },
                        onPlayerClick = onNavigateToPlayer,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                }
            }
        }
    } else {
        // =====================================================================
        // PORTRAIT MODE
        // =====================================================================
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

                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.Back,
                            contentDescription = "Kembali",
                            tint = colors.textMain,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(text = "Playlist", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                    Icon(
                        imageVector = ExpressiveIcons.More,
                        contentDescription = null,
                        tint = colors.textMain,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playlist Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlaylistCoverCard(
                            customCoverPath = playlist.customCoverPath,
                            thumbnails = tracks.map { it.thumbnailPath },
                            size = 60.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = playlist.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                            Text(
                                text = "${tracks.size} Lagu",
                                fontSize = 12.sp,
                                color = colors.textSub
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(FullPillShape)
                                    .background(colors.surfaceHigh)
                                    .clickable { /* Open PhotoPicker */ }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Camera,
                                        contentDescription = null,
                                        tint = colors.textSub,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Ubah Sampul", fontSize = 11.sp, color = colors.textSub)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hint Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ExpressiveIcons.Drag,
                            contentDescription = null,
                            tint = colors.textSub,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Geser ikon untuk atur urutan", fontSize = 11.sp, color = colors.textSub)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reorderable Track List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(tracks) { index, track ->
                        DetailTrackRow(
                            position = index + 1,
                            track = track,
                            canMoveUp = index > 0,
                            canMoveDown = index < tracks.size - 1,
                            onMoveUp = { viewModel.movePlaylistTrack(playlist.id, index, index - 1, tracks) },
                            onMoveDown = { viewModel.movePlaylistTrack(playlist.id, index, index + 1, tracks) },
                            onPlayClick = { viewModel.playTrack(track, tracks) }
                        )
                    }

                    item {
                        // Append-to-End Guide Slot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(TrackCardShape)
                                .background(colors.surfaceHigh)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = ExpressiveIcons.Plus,
                                    contentDescription = null,
                                    tint = colors.textSub,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lagu baru otomatis masuk ke urutan #${tracks.size + 1}",
                                    fontSize = 12.sp,
                                    color = colors.textSub
                                )
                            }
                        }
                    }
                }

                // Bottom Spacing
                Spacer(modifier = Modifier.height(140.dp))
            }

            // FAB "+ Tambah Lagu", Floating Player & Bottom Navigation Bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .background(colors.primary)
                        .clickable(onClick = onAddTrackClick)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ExpressiveIcons.Plus,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Tambah Lagu", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }

                val activeTrack = currentTrack
                if (activeTrack != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    ExpressiveFloatingPlayer(
                        title = activeTrack.title,
                        artist = activeTrack.producer,
                        thumbnailPath = activeTrack.thumbnailPath,
                        isPlaying = playbackState == EngineState.PLAYING,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.skipToNext() },
                        onPlayerClick = onNavigateToPlayer,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ExpressiveBottomNavigation(
                    selectedTab = NavTab.PLAYLIST,
                    onTabSelected = { tab ->
                        when (tab) {
                            NavTab.BERANDA -> onNavigateToHome()
                            NavTab.CARI -> onNavigateToSearch()
                            NavTab.PROFIL -> onNavigateToProfile()
                            NavTab.PLAYLIST -> {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailTrackRow(
    position: Int,
    track: TrackEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPlayClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
            Text(
                text = position.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSub,
                modifier = Modifier.width(22.dp)
            )

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

            Spacer(modifier = Modifier.width(10.dp))

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

            // Interactive Reorder Controls: Up / Down arrow pills + Drag Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (canMoveUp) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceHigh)
                            .clickable(onClick = onMoveUp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.ArrowUp,
                            contentDescription = "Pindah Naik",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (canMoveDown) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceHigh)
                            .clickable(onClick = onMoveDown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.ArrowDown,
                            contentDescription = "Pindah Turun",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Icon(
                    imageVector = ExpressiveIcons.Drag,
                    contentDescription = "Urutan Lagu",
                    tint = colors.textSub,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(start = 2.dp)
                )
            }
        }
    }
}
