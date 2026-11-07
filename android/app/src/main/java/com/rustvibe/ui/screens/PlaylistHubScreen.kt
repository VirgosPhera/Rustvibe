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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.rustvibe.data.local.PlaylistEntity
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

@Composable
fun PlaylistHubScreen(
    viewModel: RustVibeViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylistDetail: (String) -> Unit,
    onNavigateToOffline: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val colors = AppTheme.colors

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // =====================================================================
        // LANDSCAPE MODE (Approved Navigation Rail + 4-Column Grid)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (currentTrack != null) 68.dp else 12.dp, top = 14.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Koleksi Playlist",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textMain
                        )

                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(colors.primary)
                                .clickable { viewModel.createPlaylist("Playlist Baru") }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = ExpressiveIcons.Plus,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Buat Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Column Playlist Grid in Landscape
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 170.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(playlists) { playlist ->
                            val thumbs by viewModel.getPlaylistThumbnails(playlist.id).collectAsState(initial = emptyList())
                            HubPlaylistGridCard(
                                playlist = playlist,
                                thumbnails = thumbs,
                                onClick = { onNavigateToPlaylistDetail(playlist.id) }
                            )
                        }
                    }
                }

                // Wide Floating Mini Player at Bottom
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

                // Header + "+ Buat Baru" Button 9999px
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Koleksi Playlist",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textMain
                    )

                    Box(
                        modifier = Modifier
                            .clip(FullPillShape)
                            .background(colors.primary)
                            .clickable { viewModel.createPlaylist("Playlist Baru") }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
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
                            Text(text = "Buat Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }

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
                            .background(colors.primary)
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(text = "Playlist", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Adaptive Grid of Playlist Cards
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(playlists) { playlist ->
                        val thumbs by viewModel.getPlaylistThumbnails(playlist.id).collectAsState(initial = emptyList())
                        HubPlaylistGridCard(
                            playlist = playlist,
                            thumbnails = thumbs,
                            onClick = { onNavigateToPlaylistDetail(playlist.id) }
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
private fun HubPlaylistGridCard(
    playlist: PlaylistEntity,
    thumbnails: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            PlaylistCoverCard(
                customCoverPath = playlist.customCoverPath,
                thumbnails = thumbnails,
                size = 64.dp
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
                text = "Koleksi Lagu",
                fontSize = 12.sp,
                color = colors.textSub
            )
        }
    }
}
