package com.rustvibe.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.audio.EngineState
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.ExpressiveBottomNavigation
import com.rustvibe.ui.components.ExpressiveFloatingPlayer
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.ExpressiveNavigationRail
import com.rustvibe.ui.components.HomePlaylistItem
import com.rustvibe.ui.components.HomeTrackItem
import com.rustvibe.ui.components.NavTab
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite

@Composable
fun HomeScreen(
    viewModel: RustVibeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylist: (String) -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylistHub: () -> Unit,
    onNavigateToOffline: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val offlineTracks by viewModel.offlineTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val colors = AppTheme.colors

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // =====================================================================
        // LANDSCAPE MODE (Approved Dual-Column + Navigation Rail Layout)
        // =====================================================================
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            // Left Navigation Rail
            ExpressiveNavigationRail(
                selectedTab = NavTab.BERANDA,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.CARI -> onNavigateToSearch()
                        NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                        NavTab.PROFIL -> onNavigateToProfile()
                        NavTab.BERANDA -> {}
                    }
                },
                onToggleTheme = { viewModel.toggleTheme() },
                isDark = colors.isDark
            )

            // Right Two-Column Content Area
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
                    // Left Column: Header, Filter Chips & Playlists
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "RustVibe",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textMain
                            )
                        }

                        // Search Bar
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(FullPillShape)
                                    .background(colors.surface)
                                    .clickable(onClick = onNavigateToSearch)
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Search,
                                        contentDescription = null,
                                        tint = colors.textSub,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cari lagu atau artis...",
                                        fontSize = 12.sp,
                                        color = colors.textSub
                                    )
                                }
                            }
                        }

                        // Filter Chips
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .clip(FullPillShape)
                                        .background(colors.primary)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "Semua", fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(FullPillShape)
                                        .background(colors.surfaceHigh)
                                        .clickable(onClick = onNavigateToPlaylistHub)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "Playlist", fontSize = 11.sp, color = colors.textSub)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(FullPillShape)
                                        .background(colors.surfaceHigh)
                                        .clickable(onClick = onNavigateToOffline)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "Offline", fontSize = 11.sp, color = colors.textSub)
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Playlist",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                        }

                        items(playlists) { playlist ->
                            val thumbs by viewModel.getPlaylistThumbnails(playlist.id).collectAsState(initial = emptyList())
                            HomePlaylistItem(
                                playlist = playlist,
                                thumbnails = thumbs,
                                onClick = { onNavigateToPlaylist(playlist.id) }
                            )
                        }
                    }

                    // Right Column: Baru Diunduh Tracklist
                    LazyColumn(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Baru Diunduh",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                        }

                        items(offlineTracks) { track ->
                            HomeTrackItem(
                                track = track,
                                onPlayClick = { viewModel.playTrack(track, offlineTracks) }
                            )
                        }
                    }
                }

                // Wide Floating Player docked at bottom in landscape
                if (currentTrack != null) {
                    ExpressiveFloatingPlayer(
                        title = currentTrack!!.title,
                        artist = currentTrack!!.producer,
                        thumbnailPath = currentTrack!!.thumbnailPath,
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
        // PORTRAIT MODE (Standard Expressive Layout)
        // =====================================================================
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(
                    top = 28.dp,
                    bottom = if (currentTrack != null) 146.dp else 84.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RustVibe",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textMain
                        )

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceHigh)
                                .clickable(onClick = onNavigateToProfile),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ExpressiveIcons.Profile,
                                contentDescription = "Profil",
                                tint = colors.textSub,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(FullPillShape)
                            .background(colors.surface)
                            .clickable(onClick = onNavigateToSearch)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ExpressiveIcons.Search,
                                contentDescription = null,
                                tint = colors.textSub,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Cari lagu atau artis...",
                                fontSize = 14.sp,
                                color = colors.textSub
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(colors.primary)
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Text(text = "Semua", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
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
                                .background(colors.surfaceHigh)
                                .clickable(onClick = onNavigateToOffline)
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Text(text = "Offline", fontSize = 12.sp, color = colors.textSub)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Playlist",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(playlists) { playlist ->
                            val thumbs by viewModel.getPlaylistThumbnails(playlist.id).collectAsState(initial = emptyList())
                            HomePlaylistItem(
                                playlist = playlist,
                                thumbnails = thumbs,
                                onClick = { onNavigateToPlaylist(playlist.id) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Baru Diunduh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                }

                items(offlineTracks) { track ->
                    HomeTrackItem(
                        track = track,
                        onPlayClick = { viewModel.playTrack(track, offlineTracks) }
                    )
                }
            }

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
                            NavTab.CARI -> onNavigateToSearch()
                            NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                            NavTab.PROFIL -> onNavigateToProfile()
                            NavTab.BERANDA -> { /* already here */ }
                        }
                    }
                )
            }
        }
    }
}
