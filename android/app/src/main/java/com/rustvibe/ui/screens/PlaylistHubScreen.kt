package com.rustvibe.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.audio.EngineState
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.DeletePlaylistsConfirmDialog
import com.rustvibe.ui.components.ExpressiveBottomNavigation
import com.rustvibe.ui.components.ExpressiveFloatingPlayer
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.ExpressiveNavigationRail
import com.rustvibe.ui.components.LocalFolderImportDialog
import com.rustvibe.ui.components.NavTab
import com.rustvibe.ui.components.PlaylistCoverCard
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import com.rustvibe.ui.theme.TrackCardShape

@OptIn(ExperimentalFoundationApi::class)
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
    val scannedFolder by viewModel.scannedLocalFolder.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val colors = AppTheme.colors

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPlaylistIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.scanLocalFolder(uri)
        }
    }

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
                    if (isSelectionMode) {
                        isSelectionMode = false
                        selectedPlaylistIds = emptySet()
                    }
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
                    if (isSelectionMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceHigh)
                                        .clickable {
                                            isSelectionMode = false
                                            selectedPlaylistIds = emptySet()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = ExpressiveIcons.Close, contentDescription = "Batal", tint = colors.textMain, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${selectedPlaylistIds.size} Playlist Dipilih",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(FullPillShape)
                                        .background(colors.surfaceHigh)
                                        .clickable {
                                            selectedPlaylistIds = if (selectedPlaylistIds.size == playlists.size) emptySet() else playlists.map { it.id }.toSet()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selectedPlaylistIds.size == playlists.size) "Lepas Semua" else "Pilih Semua",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textMain
                                    )
                                }

                                if (selectedPlaylistIds.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(FullPillShape)
                                            .background(Color(0xFF52170B))
                                            .clickable { showDeleteConfirmDialog = true }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = ExpressiveIcons.Trash,
                                                contentDescription = null,
                                                tint = Color(0xFFFF897D),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Hapus (${selectedPlaylistIds.size})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF897D)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
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

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Hapus / Kelola Button
                                if (playlists.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(FullPillShape)
                                            .background(colors.surfaceHigh)
                                            .clickable { isSelectionMode = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = ExpressiveIcons.Trash,
                                                contentDescription = null,
                                                tint = colors.textSub,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Hapus", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSub)
                                        }
                                    }
                                }

                                // Import Folder Button
                                Box(
                                    modifier = Modifier
                                        .clip(FullPillShape)
                                        .background(colors.surfaceHigh)
                                        .clickable { openFolderLauncher.launch(null) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = ExpressiveIcons.FolderOpen,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Import Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                    }
                                }

                                // Buat Baru Button
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
                            val isSelected = playlist.id in selectedPlaylistIds
                            HubPlaylistGridCard(
                                playlist = playlist,
                                thumbnails = thumbs,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedPlaylistIds = if (isSelected) {
                                            selectedPlaylistIds - playlist.id
                                        } else {
                                            selectedPlaylistIds + playlist.id
                                        }
                                    } else {
                                        onNavigateToPlaylistDetail(playlist.id)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedPlaylistIds = setOf(playlist.id)
                                    }
                                }
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

                // Header
                if (isSelectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceHigh)
                                    .clickable {
                                        isSelectionMode = false
                                        selectedPlaylistIds = emptySet()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = ExpressiveIcons.Close, contentDescription = "Batal", tint = colors.textMain, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${selectedPlaylistIds.size} Dipilih",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain
                                )
                                Text(
                                    text = "Ketuk playlist untuk memilih",
                                    fontSize = 11.sp,
                                    color = colors.textSub
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(colors.surfaceHigh)
                                .clickable {
                                    selectedPlaylistIds = if (selectedPlaylistIds.size == playlists.size) emptySet() else playlists.map { it.id }.toSet()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedPlaylistIds.size == playlists.size) "Batal Semua" else "Pilih Semua",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlist",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textMain
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Hapus / Kelola Button
                            if (playlists.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(FullPillShape)
                                        .background(colors.surfaceHigh)
                                        .clickable { isSelectionMode = true }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = ExpressiveIcons.Trash,
                                            contentDescription = null,
                                            tint = colors.textSub,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Hapus", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSub)
                                    }
                                }
                            }

                            // Import Folder Button
                            Box(
                                modifier = Modifier
                                    .clip(FullPillShape)
                                    .background(colors.surfaceHigh)
                                    .clickable { openFolderLauncher.launch(null) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.FolderOpen,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Import", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                }
                            }

                            // Buat Baru Button
                            Box(
                                modifier = Modifier
                                    .clip(FullPillShape)
                                    .background(colors.primary)
                                    .clickable { viewModel.createPlaylist("Playlist Baru") }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Plus,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Chips
                if (!isSelectionMode) {
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
                }

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
                        val isSelected = playlist.id in selectedPlaylistIds
                        HubPlaylistGridCard(
                            playlist = playlist,
                            thumbnails = thumbs,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) {
                                    selectedPlaylistIds = if (isSelected) {
                                        selectedPlaylistIds - playlist.id
                                    } else {
                                        selectedPlaylistIds + playlist.id
                                    }
                                } else {
                                    onNavigateToPlaylistDetail(playlist.id)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                    selectedPlaylistIds = setOf(playlist.id)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (currentTrack != null || isSelectionMode) 146.dp else 84.dp))
            }

            // Bottom Actions (Selection Delete Bar OR Floating Mini Player + Bottom Nav)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (isSelectionMode) {
                    if (selectedPlaylistIds.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(FullPillShape)
                                .background(Color(0xFF52170B))
                                .clickable { showDeleteConfirmDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = ExpressiveIcons.Trash,
                                    contentDescription = null,
                                    tint = Color(0xFFFF897D),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hapus ${selectedPlaylistIds.size} Playlist Terpilih",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF897D)
                                )
                            }
                        }
                    }
                } else if (currentTrack != null) {
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
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedPlaylistIds = emptySet()
                        }
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

    // Modal Dialog: Local Folder Import
    scannedFolder?.let { folder ->
        LocalFolderImportDialog(
            scannedFolder = folder,
            onConfirm = { name -> viewModel.confirmImportLocalFolder(folder, name) },
            onDismiss = { viewModel.dismissLocalFolderDialog() }
        )
    }

    // Modal Dialog: Delete Playlists Confirmation
    if (showDeleteConfirmDialog) {
        val selectedObjects = playlists.filter { it.id in selectedPlaylistIds }
        DeletePlaylistsConfirmDialog(
            selectedPlaylists = selectedObjects,
            onConfirmDelete = {
                viewModel.deletePlaylists(selectedPlaylistIds)
                selectedPlaylistIds = emptySet()
                isSelectionMode = false
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HubPlaylistGridCard(
    playlist: PlaylistEntity,
    thumbnails: List<String> = emptyList(),
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val cardBg = if (isSelected) Color(0xFF52170B) else colors.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(TrackCardShape)
            .background(cardBg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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

        // Selection Checkmark Badge
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.primary else colors.surfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = ExpressiveIcons.Check,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
