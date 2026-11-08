package com.rustvibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.screens.HomeScreen
import com.rustvibe.ui.screens.OfflineScreen
import com.rustvibe.ui.screens.PlayerScreen
import com.rustvibe.ui.screens.PlaylistDetailScreen
import com.rustvibe.ui.screens.PlaylistHubScreen
import com.rustvibe.ui.screens.ProfileScreen
import com.rustvibe.ui.screens.SearchScreen
import com.rustvibe.ui.theme.RustVibeTheme

import androidx.compose.ui.graphics.toArgb

enum class ScreenDestination {
    HOME,
    SEARCH,
    PLAYER,
    PLAYLIST_DETAIL,
    PLAYLIST_HUB,
    OFFLINE,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private val viewModel: RustVibeViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("MainActivity", "POST_NOTIFICATIONS granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            RustVibeTheme(themeMode = themeMode) {
                val colors = com.rustvibe.ui.theme.AppTheme.colors
                androidx.compose.runtime.SideEffect {
                    val bgInt = colors.bg.toArgb()
                    val surfInt = colors.surface.toArgb()
                    window.statusBarColor = bgInt
                    window.navigationBarColor = surfInt
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !colors.isDark
                        isAppearanceLightNavigationBars = !colors.isDark
                    }
                }
                MainAppNavHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppNavHost(viewModel: RustVibeViewModel) {
    var currentScreen by remember { mutableStateOf(ScreenDestination.HOME) }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    val playlists by viewModel.playlists.collectAsState()

    // Handle system back gesture
    if (currentScreen != ScreenDestination.HOME) {
        androidx.activity.compose.BackHandler {
            if (currentScreen == ScreenDestination.PLAYLIST_DETAIL) {
                currentScreen = ScreenDestination.PLAYLIST_HUB
            } else {
                currentScreen = ScreenDestination.HOME
            }
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == ScreenDestination.PLAYER) {
                // Slide in vertically up
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            } else if (initialState == ScreenDestination.PLAYER) {
                // Slide out vertically down
                slideInVertically { height -> -height } + fadeIn() togetherWith
                        slideOutVertically { height -> height } + fadeOut()
            } else {
                // Instant switch between tabs - Zero delay, zero fade, pure native speed!
                androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            ScreenDestination.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { currentScreen = ScreenDestination.SEARCH },
                    onNavigateToPlaylist = { id ->
                        selectedPlaylistId = id
                        currentScreen = ScreenDestination.PLAYLIST_DETAIL
                    },
                    onNavigateToPlayer = { currentScreen = ScreenDestination.PLAYER },
                    onNavigateToPlaylistHub = { currentScreen = ScreenDestination.PLAYLIST_HUB },
                    onNavigateToOffline = { currentScreen = ScreenDestination.OFFLINE },
                    onNavigateToProfile = { currentScreen = ScreenDestination.PROFILE }
                )
            }

            ScreenDestination.SEARCH -> {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { currentScreen = ScreenDestination.HOME },
                    onNavigateToPlaylistHub = { currentScreen = ScreenDestination.PLAYLIST_HUB },
                    onNavigateToProfile = { currentScreen = ScreenDestination.PROFILE },
                    onNavigateToPlayer = { currentScreen = ScreenDestination.PLAYER },
                    onNavigateToPlaylistDetail = { id ->
                        selectedPlaylistId = id
                        currentScreen = ScreenDestination.PLAYLIST_DETAIL
                    }
                )
            }

            ScreenDestination.PLAYER -> {
                PlayerScreen(
                    viewModel = viewModel,
                    onBackClick = { currentScreen = ScreenDestination.HOME }
                )
            }

            ScreenDestination.PLAYLIST_DETAIL -> {
                val playlist = playlists.find { it.id == selectedPlaylistId }
                    ?: playlists.firstOrNull()
                    ?: com.rustvibe.data.local.PlaylistEntity("temp", "Playlist")

                val playlistTracks by viewModel.getTracksForPlaylist(playlist.id)
                    .collectAsState(initial = emptyList())

                PlaylistDetailScreen(
                    playlist = playlist,
                    tracks = playlistTracks,
                    viewModel = viewModel,
                    onBackClick = { currentScreen = ScreenDestination.PLAYLIST_HUB },
                    onAddTrackClick = { currentScreen = ScreenDestination.SEARCH },
                    onNavigateToHome = { currentScreen = ScreenDestination.HOME },
                    onNavigateToSearch = { currentScreen = ScreenDestination.SEARCH },
                    onNavigateToProfile = { currentScreen = ScreenDestination.PROFILE },
                    onNavigateToPlayer = { currentScreen = ScreenDestination.PLAYER }
                )
            }

            ScreenDestination.PLAYLIST_HUB -> {
                PlaylistHubScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { currentScreen = ScreenDestination.HOME },
                    onNavigateToSearch = { currentScreen = ScreenDestination.SEARCH },
                    onNavigateToPlaylistDetail = { id ->
                        selectedPlaylistId = id
                        currentScreen = ScreenDestination.PLAYLIST_DETAIL
                    },
                    onNavigateToOffline = { currentScreen = ScreenDestination.OFFLINE },
                    onNavigateToProfile = { currentScreen = ScreenDestination.PROFILE },
                    onNavigateToPlayer = { currentScreen = ScreenDestination.PLAYER }
                )
            }

            ScreenDestination.OFFLINE -> {
                OfflineScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { currentScreen = ScreenDestination.HOME },
                    onNavigateToSearch = { currentScreen = ScreenDestination.SEARCH },
                    onNavigateToPlaylistHub = { currentScreen = ScreenDestination.PLAYLIST_HUB },
                    onNavigateToProfile = { currentScreen = ScreenDestination.PROFILE },
                    onNavigateToPlayer = { currentScreen = ScreenDestination.PLAYER }
                )
            }

            ScreenDestination.PROFILE -> {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { currentScreen = ScreenDestination.HOME },
                    onNavigateToSearch = { currentScreen = ScreenDestination.SEARCH },
                    onNavigateToPlaylistHub = { currentScreen = ScreenDestination.PLAYLIST_HUB },
                    onNavigateToPlayer = { currentScreen = ScreenDestination.PLAYER }
                )
            }
        }
    }
}
