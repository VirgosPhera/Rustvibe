package com.rustvibe.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rustvibe.audio.EngineState
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.data.remote.DownloadProgressState
import com.rustvibe.data.remote.OnlinePlaylistResult
import com.rustvibe.data.remote.SongSearchResult
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.DownloadTriageBottomSheet
import com.rustvibe.ui.components.ExpressiveBottomNavigation
import com.rustvibe.ui.components.ExpressiveFloatingPlayer
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.ExpressiveNavigationRail
import com.rustvibe.ui.components.LocalFolderImportDialog
import com.rustvibe.ui.components.NavTab
import com.rustvibe.ui.components.PlaylistCoverCard
import com.rustvibe.ui.components.PlaylistDownloadDialog
import com.rustvibe.ui.components.PlaylistPickerBottomSheet
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import com.rustvibe.ui.theme.TrackCardShape

@Composable
fun SearchScreen(
    viewModel: RustVibeViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToPlaylistHub: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlayer: () -> Unit = {},
    onNavigateToPlaylistDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val pendingPackage by viewModel.pendingDownloadedPackage.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val detectedPlaylist by viewModel.detectedPlaylist.collectAsState()
    val onlinePlaylists by viewModel.onlinePlaylists.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val scannedFolder by viewModel.scannedLocalFolder.collectAsState()

    val matchingPlaylists = if (searchQuery.isBlank()) playlists else playlists.filter { it.name.contains(searchQuery, ignoreCase = true) }

    var showPlaylistPicker by remember { mutableStateOf(false) }
    val colors = AppTheme.colors

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // =====================================================================
        // LANDSCAPE MODE (Approved Dual-Column Search with Live Progress Card)
        // =====================================================================
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            // Left Navigation Rail
            ExpressiveNavigationRail(
                selectedTab = NavTab.CARI,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.BERANDA -> onNavigateToHome()
                        NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                        NavTab.PROFIL -> onNavigateToProfile()
                        NavTab.CARI -> {}
                    }
                },
                onToggleTheme = { viewModel.toggleTheme() },
                isDark = colors.isDark
            )

            // Right Main Content Area
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
                    // Search Bar 9999px (Wide in landscape)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(FullPillShape)
                            .background(colors.surface)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = ExpressiveIcons.Search,
                                contentDescription = null,
                                tint = colors.textSub,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    viewModel.searchSongs(it)
                                },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = colors.textMain,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                                ),
                                cursorBrush = SolidColor(colors.primary),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = ExpressiveIcons.Close,
                                    contentDescription = "Hapus",
                                    tint = colors.textSub,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Two Columns: Results on Left, Dedicated Download Card on Right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: Results
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Text(
                                text = "Hasil Pencarian",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSub
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isSearching && onlinePlaylists.isEmpty() && searchResults.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(32.dp))
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (matchingPlaylists.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Playlist Ditemukan",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textSub
                                            )
                                        }
                                        items(matchingPlaylists) { pl ->
                                            val thumbs by viewModel.getPlaylistThumbnails(pl.id).collectAsState(initial = emptyList())
                                            PlaylistSearchCard(
                                                playlist = pl,
                                                thumbnails = thumbs,
                                                onClick = { onNavigateToPlaylistDetail(pl.id) }
                                            )
                                        }
                                    }
                                    if (onlinePlaylists.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Playlist YouTube (${onlinePlaylists.size})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primary
                                            )
                                        }
                                        items(onlinePlaylists) { opl ->
                                            OnlinePlaylistSearchCard(
                                                playlist = opl,
                                                onClick = { viewModel.selectOnlinePlaylist(opl) }
                                            )
                                        }
                                    }
                                    items(searchResults) { item ->
                                        SearchResultRow(
                                            item = item,
                                            isDownloading = downloadProgress?.isDownloading == true && downloadProgress?.trackId == item.id,
                                            downloadPercent = if (downloadProgress?.trackId == item.id) downloadProgress?.percent ?: 0 else 0,
                                            onDownloadClick = { viewModel.downloadSong(item) }
                                        )
                                    }
                                }
                            }
                        }

                        // Right Column: Dedicated Live Download Progress Card
                        Column(modifier = Modifier.weight(1.05f).fillMaxHeight()) {
                            Text(
                                text = "Status Unduhan Berjalan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSub
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            DownloadStatusCard(
                                progressState = downloadProgress,
                                colors = colors
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
        // PORTRAIT MODE (With Live Progress Card & Bottom Nav)
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

                // Search Bar Input 9999px
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(FullPillShape)
                        .background(colors.surface)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.Search,
                            contentDescription = null,
                            tint = colors.textSub,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchSongs(it)
                            },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = colors.textMain,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            ),
                            cursorBrush = SolidColor(colors.primary),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = ExpressiveIcons.Close,
                                contentDescription = "Hapus",
                                tint = colors.textSub,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Download Progress Banner in Portrait
                AnimatedVisibility(visible = downloadProgress != null) {
                    downloadProgress?.let { p ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${p.percent}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // 9999px Capsule Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(FullPillShape)
                                    .background(colors.surfaceHigh)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(p.progress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(FullPillShape)
                                        .background(colors.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = p.statusText,
                                fontSize = 11.sp,
                                color = colors.textSub
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Playlist", "Lagu", "M4A").forEach { chip ->
                        val isSelected = selectedFilter == chip
                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(if (isSelected) colors.primary else colors.surface)
                                .clickable { selectedFilter = chip }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chip,
                                fontSize = 11.sp,
                                color = if (isSelected) PureWhite else colors.textSub,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSearching && onlinePlaylists.isEmpty() && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Playlists section if filter is Semua or Playlist
                        if (selectedFilter == "Semua" || selectedFilter == "Playlist") {
                            if (matchingPlaylists.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Koleksi Playlist (${matchingPlaylists.size})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textSub,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                    )
                                }
                                items(matchingPlaylists) { pl ->
                                    val thumbs by viewModel.getPlaylistThumbnails(pl.id).collectAsState(initial = emptyList())
                                    PlaylistSearchCard(
                                        playlist = pl,
                                        thumbnails = thumbs,
                                        onClick = { onNavigateToPlaylistDetail(pl.id) }
                                    )
                                }
                            }

                            if (onlinePlaylists.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Playlist YouTube (${onlinePlaylists.size})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                    )
                                }
                                items(onlinePlaylists) { opl ->
                                    OnlinePlaylistSearchCard(
                                        playlist = opl,
                                        onClick = { viewModel.selectOnlinePlaylist(opl) }
                                    )
                                }
                            }
                        }

                        // Songs section if filter is NOT Playlist only
                        if (selectedFilter != "Playlist") {
                            if (searchResults.isNotEmpty()) {
                                if (selectedFilter == "Semua" && matchingPlaylists.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Lagu Ditemukan",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textSub,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                        )
                                    }
                                }
                                items(searchResults) { item ->
                                    SearchResultRow(
                                        item = item,
                                        isDownloading = downloadProgress?.isDownloading == true && downloadProgress?.trackId == item.id,
                                        downloadPercent = if (downloadProgress?.trackId == item.id) downloadProgress?.percent ?: 0 else 0,
                                        onDownloadClick = { viewModel.downloadSong(item) }
                                    )
                                }
                            }
                        }
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
                    selectedTab = NavTab.CARI,
                    onTabSelected = { tab ->
                        when (tab) {
                            NavTab.BERANDA -> onNavigateToHome()
                            NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                            NavTab.PROFIL -> onNavigateToProfile()
                            NavTab.CARI -> {}
                        }
                    }
                )
            }
        }
    }

    // Modal Dialog: Download 1 Full Playlist with Custom Rename
    detectedPlaylist?.let { pl ->
        PlaylistDownloadDialog(
            playlist = pl,
            onConfirm = { customName ->
                viewModel.confirmDownloadPlaylist(pl, customName)
            },
            onDismiss = {
                viewModel.dismissPlaylistDialog()
            }
        )
    }

    // Modal Dialog: Local Folder Import
    scannedFolder?.let { folder ->
        LocalFolderImportDialog(
            scannedFolder = folder,
            onConfirm = { name -> viewModel.confirmImportLocalFolder(folder, name) },
            onDismiss = { viewModel.dismissLocalFolderDialog() }
        )
    }

    // Modal Bottom Sheet: Post-Download Triage Flow
    if (pendingPackage != null && !showPlaylistPicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x991A1012)),
            contentAlignment = Alignment.BottomCenter
        ) {
            DownloadTriageBottomSheet(
                onSaveToPlaylistClick = {
                    if (playlists.isNotEmpty()) {
                        showPlaylistPicker = true
                    } else {
                        viewModel.createPlaylist("Playlist Saya")
                        showPlaylistPicker = true
                    }
                },
                onEphemeralListenClick = {
                    viewModel.confirmEphemeralListen()
                },
                onCancelClick = {
                    viewModel.dismissTriage()
                }
            )
        }
    }

    // Modal Bottom Sheet: Select Playlist Flow
    if (pendingPackage != null && showPlaylistPicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x991A1012)),
            contentAlignment = Alignment.BottomCenter
        ) {
            PlaylistPickerBottomSheet(
                playlists = playlists,
                onSelectPlaylist = { playlistId ->
                    viewModel.confirmSaveToPlaylist(playlistId)
                    showPlaylistPicker = false
                },
                onCancel = {
                    showPlaylistPicker = false
                }
            )
        }
    }
}

@Composable
private fun DownloadStatusCard(
    progressState: DownloadProgressState?,
    colors: com.rustvibe.ui.theme.ExpressiveThemeColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(16.dp)
    ) {
        if (progressState != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (progressState.isDownloading) "Mengunduh Aliran Audio..." else "Unduhan Berhasil!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
                Text(
                    text = "${progressState.title} • Format M4A Asli",
                    fontSize = 12.sp,
                    color = colors.textSub,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Large 9999px Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(FullPillShape)
                        .background(colors.surfaceHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressState.progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(FullPillShape)
                            .background(colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progressState.percent}% Selesai",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        text = progressState.speedText,
                        fontSize = 11.sp,
                        color = colors.textSub
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Sampul HD Terunduh  •  ✓ Lirik LRCLIB Sinkron OK",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ExpressiveIcons.Download,
                    contentDescription = null,
                    tint = colors.textSub,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Belum ada unduhan berjalan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSub
                )
                Text(
                    text = "Klik 'Unduh' pada lagu atau playlist",
                    fontSize = 11.sp,
                    color = colors.textSub
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: SongSearchResult,
    isDownloading: Boolean = false,
    downloadPercent: Int = 0,
    onDownloadClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = ExpressiveIcons.Music,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    maxLines = 1
                )
                Text(
                    text = "${item.producer} • ${item.durationSeconds / 60}:${String.format("%02d", item.durationSeconds % 60)}",
                    fontSize = 12.sp,
                    color = colors.textSub
                )
            }

            if (isDownloading) {
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .background(colors.primary)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$downloadPercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .background(colors.primary)
                        .clickable(onClick = onDownloadClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ExpressiveIcons.Download,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Unduh", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSearchCard(
    playlist: PlaylistEntity,
    thumbnails: List<String>,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaylistCoverCard(
                customCoverPath = playlist.customCoverPath,
                thumbnails = thumbnails,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    maxLines = 1
                )
                Text(
                    text = "Koleksi Playlist",
                    fontSize = 11.sp,
                    color = colors.textSub
                )
            }

            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Buka",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun OnlinePlaylistSearchCard(
    playlist: OnlinePlaylistResult,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = playlist.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = ExpressiveIcons.Playlist,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    maxLines = 1
                )
                Text(
                    text = "${playlist.uploader} • Playlist YouTube",
                    fontSize = 11.sp,
                    color = colors.textSub
                )
            }

            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .background(colors.primary)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ExpressiveIcons.Download,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Unduh",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }
    }
}
