package com.rustvibe.ui.screens

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.audio.EngineState
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.ExpressiveBottomNavigation
import com.rustvibe.ui.components.ExpressiveFloatingPlayer
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.ExpressiveNavigationRail
import com.rustvibe.ui.components.NavTab
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import com.rustvibe.ui.theme.TrackCardShape

@Composable
fun ProfileScreen(
    viewModel: RustVibeViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylistHub: () -> Unit,
    onNavigateToPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var autoPurgeEphemeral by remember { mutableStateOf(true) }
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val colors = AppTheme.colors

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // =====================================================================
        // LANDSCAPE MODE (Approved Navigation Rail + 2-Column Settings)
        // =====================================================================
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            ExpressiveNavigationRail(
                selectedTab = NavTab.PROFIL,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.BERANDA -> onNavigateToHome()
                        NavTab.CARI -> onNavigateToSearch()
                        NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                        NavTab.PROFIL -> {}
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
                    Text(
                        text = "Profil & Pengaturan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textMain
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Column 1 (Left): User card, Theme Switcher, Audio Engine
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                // User Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(TrackCardShape)
                                        .background(colors.surface)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = ExpressiveIcons.Profile,
                                                contentDescription = null,
                                                tint = colors.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = "Only-One-Kind", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                            Text(text = "RustVibe Standalone • Zero Ads", fontSize = 11.sp, color = colors.textSub)
                                        }
                                    }
                                }
                            }

                            item {
                                // Theme Selector Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(TrackCardShape)
                                        .background(colors.surface)
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(text = "Tampilan & Tema", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                        Text(text = themeMode.subtitle, fontSize = 11.sp, color = colors.textSub)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val isDark = themeMode == com.rustvibe.ui.theme.ThemeMode.DARK
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(FullPillShape)
                                                    .background(if (isDark) colors.primary else colors.surfaceHigh)
                                                    .clickable { viewModel.setThemeMode(com.rustvibe.ui.theme.ThemeMode.DARK) }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "Gelap", fontSize = 11.sp, color = if (isDark) PureWhite else colors.textSub, fontWeight = FontWeight.Bold)
                                            }

                                            val isLight = themeMode == com.rustvibe.ui.theme.ThemeMode.LIGHT
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(FullPillShape)
                                                    .background(if (isLight) colors.primary else colors.surfaceHigh)
                                                    .clickable { viewModel.setThemeMode(com.rustvibe.ui.theme.ThemeMode.LIGHT) }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "Terang", fontSize = 11.sp, color = if (isLight) PureWhite else colors.textSub, fontWeight = FontWeight.Bold)
                                            }

                                            val isDynamic = themeMode == com.rustvibe.ui.theme.ThemeMode.DYNAMIC_SYSTEM
                                            Box(
                                                modifier = Modifier
                                                    .weight(1.3f)
                                                    .clip(FullPillShape)
                                                    .background(if (isDynamic) colors.primary else colors.surfaceHigh)
                                                    .clickable { viewModel.setThemeMode(com.rustvibe.ui.theme.ThemeMode.DYNAMIC_SYSTEM) }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "Aksen Android", fontSize = 11.sp, color = if (isDynamic) PureWhite else colors.textSub, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                SettingItemWithBadge(
                                    title = "Driver Audio",
                                    subtitle = "AAudio Low-Latency",
                                    badgeText = "Aktif"
                                )
                            }
                        }

                        // Column 2 (Right): Storage, Cache, About
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(TrackCardShape)
                                        .background(colors.surface)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(text = "Penyimpanan & Cache", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                        Text(text = "Kapasitas saat ini: 18 MB dari 6 Lagu", fontSize = 11.sp, color = colors.textSub)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(FullPillShape)
                                                .background(colors.surfaceHigh)
                                                .clickable { /* clean */ }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = ExpressiveIcons.Trash, contentDescription = null, tint = colors.textSub, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = "Bersihkan Cache", fontSize = 11.sp, color = colors.textSub)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(TrackCardShape)
                                        .background(colors.surface)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(text = "RustVibe v1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                        Text(text = "Native Rust Audio Engine • Standalone yt-dlp", fontSize = 11.sp, color = colors.textSub)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Desain Responsif 20:9 Redmi Note 10 Pro", fontSize = 11.sp, color = colors.textSub)
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Tautan Proyek & Media Resmi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSub
                                )
                            }

                            // GitHub Link Card (Landscape)
                            item {
                                val context = LocalContext.current
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clip(FullPillShape)
                                        .background(colors.surface)
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Only-One-Kind/rustvibe-music")).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(colors.surfaceHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = ExpressiveIcons.GitHub,
                                                contentDescription = "GitHub",
                                                tint = PureWhite,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "GitHub Repository ⭐",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textMain
                                            )
                                            Text(
                                                text = "Kasih bintangnya dong biar makin populer nih aplikasi, Makasih!",
                                                fontSize = 9.5.sp,
                                                color = colors.primary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        }

                                        Icon(
                                            imageVector = ExpressiveIcons.OpenLink,
                                            contentDescription = "Buka",
                                            tint = colors.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // YouTube Link Card (Landscape)
                            item {
                                val context = LocalContext.current
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clip(FullPillShape)
                                        .background(colors.surface)
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@lampulolhst")).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF0000)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = ExpressiveIcons.YouTube,
                                                contentDescription = "YouTube",
                                                tint = PureWhite,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Channel YouTube Resmi",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textMain
                                            )
                                            Text(
                                                text = "@lampulolhst • Kunjungi Channel & Playlist Official",
                                                fontSize = 10.sp,
                                                color = colors.textSub
                                            )
                                        }

                                        Icon(
                                            imageVector = ExpressiveIcons.OpenLink,
                                            contentDescription = "Buka",
                                            tint = colors.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
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
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Header
                Text(
                    text = "Profil & Pengaturan",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Profile Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(TrackCardShape)
                        .background(colors.surface)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.accentContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ExpressiveIcons.Profile,
                                contentDescription = "Profil",
                                tint = colors.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Only-One-Kind",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                            Text(
                                text = "RustVibe Standalone • Zero Ads",
                                fontSize = 12.sp,
                                color = colors.textSub
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION: Tampilan & Tema (Theme Switcher)
                Text(
                    text = "Tampilan & Tema",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(TrackCardShape)
                        .background(colors.surface)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = themeMode.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain
                                )
                                Text(
                                    text = themeMode.subtitle,
                                    fontSize = 11.sp,
                                    color = colors.textSub
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3-way Theme Selection Chips (9999px pills)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Dark Pill
                            val isDarkActive = themeMode == com.rustvibe.ui.theme.ThemeMode.DARK
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(FullPillShape)
                                    .background(if (isDarkActive) colors.primary else colors.surfaceHigh)
                                    .clickable { viewModel.setThemeMode(com.rustvibe.ui.theme.ThemeMode.DARK) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Moon,
                                        contentDescription = null,
                                        tint = if (isDarkActive) PureWhite else colors.textSub,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Gelap",
                                        fontSize = 11.sp,
                                        fontWeight = if (isDarkActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDarkActive) PureWhite else colors.textSub
                                    )
                                }
                            }

                            // Light Pill
                            val isLightActive = themeMode == com.rustvibe.ui.theme.ThemeMode.LIGHT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(FullPillShape)
                                    .background(if (isLightActive) colors.primary else colors.surfaceHigh)
                                    .clickable { viewModel.setThemeMode(com.rustvibe.ui.theme.ThemeMode.LIGHT) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Sun,
                                        contentDescription = null,
                                        tint = if (isLightActive) PureWhite else colors.textSub,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Terang",
                                        fontSize = 11.sp,
                                        fontWeight = if (isLightActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLightActive) PureWhite else colors.textSub
                                    )
                                }
                            }

                            // Dynamic System Accent (Android 15-16 Monet)
                            val isDynamicActive = themeMode == com.rustvibe.ui.theme.ThemeMode.DYNAMIC_SYSTEM
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clip(FullPillShape)
                                    .background(if (isDynamicActive) colors.primary else colors.surfaceHigh)
                                    .clickable { viewModel.setThemeMode(com.rustvibe.ui.theme.ThemeMode.DYNAMIC_SYSTEM) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Bolt,
                                        contentDescription = null,
                                        tint = if (isDynamicActive) PureWhite else colors.textSub,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Aksen Android",
                                        fontSize = 11.sp,
                                        fontWeight = if (isDynamicActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDynamicActive) PureWhite else colors.textSub
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 1: Mesin Audio Rust
                Text(
                    text = "Mesin Audio Rust",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingItemWithBadge(
                    title = "Driver Audio",
                    subtitle = "AAudio Low-Latency",
                    badgeText = "Aktif"
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingItemWithBadge(
                    title = "Format Utama",
                    subtitle = "M4A (MP3 Backup Siap)",
                    badgeText = "256k"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Penyimpanan & Cache
                Text(
                    text = "Penyimpanan & Cache",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Setting Item: Auto purge toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(TrackCardShape)
                        .background(colors.surface)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Hapus Dengar Sekali", fontSize = 14.sp, color = colors.textMain)
                            Text(text = "Otomatis musnahkan cache", fontSize = 11.sp, color = colors.textSub)
                        }

                        Switch(
                            checked = autoPurgeEphemeral,
                            onCheckedChange = { autoPurgeEphemeral = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.textSub,
                                uncheckedTrackColor = colors.surfaceHigh
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Setting Item: Clear Cache
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(TrackCardShape)
                        .background(colors.surface)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Kapasitas Cache", fontSize = 14.sp, color = colors.textMain)
                            Text(text = "12 MB File Sementara", fontSize = 11.sp, color = colors.textSub)
                        }

                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(colors.surfaceHigh)
                                .clickable { /* Purge cache */ }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = ExpressiveIcons.Trash,
                                    contentDescription = null,
                                    tint = colors.textSub,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Bersihkan", fontSize = 11.sp, color = colors.textSub)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: Info Aplikasi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(TrackCardShape)
                        .background(colors.surface)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(text = "RustVibe v1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                        Text(text = "Pure Rust Audio • Standalone yt-dlp", fontSize = 11.sp, color = colors.textSub)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 4: Tautan Proyek & Media Resmi (Portrait)
                Text(
                    text = "Tautan Proyek & Media Resmi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                val context = LocalContext.current

                // 1. GitHub Repo Button (9999px Full Pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(TrackCardShape)
                        .background(colors.surface)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/VirgosPhera/Rustvibe")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ExpressiveIcons.GitHub,
                                contentDescription = "GitHub",
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GitHub Repository ⭐",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                            Text(
                                text = "Kasih bintangnya dong biar makin populer nih aplikasi, Makasih!",
                                fontSize = 10.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = ExpressiveIcons.OpenLink,
                            contentDescription = "Buka Link",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. YouTube Channel Button (9999px Full Pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(FullPillShape)
                        .background(colors.surface)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@lampulolhst")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF0000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ExpressiveIcons.YouTube,
                                contentDescription = "YouTube",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Channel YouTube Resmi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                            Text(
                                text = "@lampulolhst • Kunjungi Channel & Playlist Official",
                                fontSize = 11.sp,
                                color = colors.textSub
                            )
                        }

                        Icon(
                            imageVector = ExpressiveIcons.OpenLink,
                            contentDescription = "Buka Link",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (currentTrack != null) 220.dp else 160.dp))
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
                    selectedTab = NavTab.PROFIL,
                    onTabSelected = { tab ->
                        when (tab) {
                            NavTab.BERANDA -> onNavigateToHome()
                            NavTab.CARI -> onNavigateToSearch()
                            NavTab.PLAYLIST -> onNavigateToPlaylistHub()
                            NavTab.PROFIL -> {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingItemWithBadge(
    title: String,
    subtitle: String,
    badgeText: String
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(TrackCardShape)
            .background(colors.surface)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 14.sp, color = colors.textMain)
                Text(text = subtitle, fontSize = 11.sp, color = colors.textSub)
            }

            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(text = badgeText, fontSize = 11.sp, color = colors.secondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
