package com.rustvibe.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rustvibe.audio.EngineState
import com.rustvibe.ui.RustVibeViewModel
import com.rustvibe.ui.components.ExpressiveIcons
import com.rustvibe.ui.components.SyncedLyricsStage
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import java.io.File

@Composable
fun PlayerScreen(
    viewModel: RustVibeViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track by viewModel.currentTrack.collectAsState()
    val lyrics by viewModel.currentLyrics.collectAsState()
    val positionMs by viewModel.currentPositionMs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isUsingBackup by viewModel.isUsingBackup.collectAsState()
    val colors = AppTheme.colors

    val totalDurationMs = ((track?.durationSeconds ?: 0) * 1000).toLong()
    var isLyricsExpanded by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat().coerceAtLeast(1f) }
    var currentVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            }
        }
        context.registerReceiver(receiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {}
        }
    }

    if (isLandscape) {
        // =====================================================================
        // LANDSCAPE MODE (Approved Dual-Pane Layout)
        // =====================================================================
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Pane: Controls, Metadata, Scrubber, Volume
            Column(
                modifier = Modifier
                    .weight(if (isLyricsExpanded) 1f else 1.4f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceHigh)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = ExpressiveIcons.Back, contentDescription = "Kembali", tint = colors.textMain, modifier = Modifier.size(20.dp))
                    }

                    Text(text = "Sedang Diputar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)

                    Box(
                        modifier = Modifier
                            .clip(FullPillShape)
                            .background(if (isLyricsExpanded) colors.primary else colors.surfaceHigh)
                            .clickable { isLyricsExpanded = !isLyricsExpanded }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLyricsExpanded) ExpressiveIcons.ChevronDown else ExpressiveIcons.ChevronUp,
                                contentDescription = null,
                                tint = if (isLyricsExpanded) PureWhite else colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLyricsExpanded) "Tutup Lirik" else "Buka Lirik",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLyricsExpanded) PureWhite else colors.primary
                            )
                        }
                    }
                }

                // Album Art + Metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isLyricsExpanded) 88.dp else 110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!track?.thumbnailPath.isNullOrEmpty() && File(track!!.thumbnailPath).exists()) {
                            AsyncImage(
                                model = File(track!!.thumbnailPath),
                                contentDescription = "Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(imageVector = ExpressiveIcons.Music, contentDescription = null, tint = colors.primary, modifier = Modifier.size(44.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track?.title ?: "Memuat Lagu...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain,
                            maxLines = 1
                        )
                        Text(
                            text = track?.producer ?: "Artis",
                            fontSize = 12.sp,
                            color = colors.textSub,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(colors.surfaceHigh)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isUsingBackup) "MP3 FAILOVER" else "M4A",
                                fontSize = 9.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Scrubber Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (totalDurationMs > 0) (positionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { frac -> viewModel.seekTo((frac * totalDurationMs).toLong()) },
                        colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary, inactiveTrackColor = colors.surfaceHigh),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = formatMs(positionMs), fontSize = 10.sp, color = colors.textSub)
                        Text(text = formatMs(totalDurationMs), fontSize = 10.sp, color = colors.textSub)
                    }
                }

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = ExpressiveIcons.Shuffle, contentDescription = "Shuffle", tint = colors.textSub, modifier = Modifier.size(22.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.skipToPrev() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = ExpressiveIcons.Prev, contentDescription = "Prev", tint = colors.textMain, modifier = Modifier.size(26.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playbackState == EngineState.PLAYING) ExpressiveIcons.Pause else ExpressiveIcons.Play,
                            contentDescription = "Play/Pause",
                            tint = PureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.skipToNext() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = ExpressiveIcons.Next, contentDescription = "Next", tint = colors.textMain, modifier = Modifier.size(26.dp))
                    }
                    Icon(imageVector = ExpressiveIcons.Repeat, contentDescription = "Repeat", tint = colors.textSub, modifier = Modifier.size(22.dp))
                }

                // Volume Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(38.dp)
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.VolumeDown,
                            contentDescription = null,
                            tint = colors.textSub,
                            modifier = Modifier.size(16.dp).clickable {
                                val newVol = (currentVolume - 1).coerceAtLeast(0f)
                                currentVolume = newVol
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                            }
                        )
                        val volumePercent = ((currentVolume / maxVolume) * 100).toInt().coerceIn(0, 100)

                        Slider(
                            value = (currentVolume / maxVolume).coerceIn(0f, 1f),
                            onValueChange = { frac ->
                                val newVol = frac * maxVolume
                                currentVolume = newVol
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                            },
                            colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary, inactiveTrackColor = colors.surface),
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                        )

                        Text(
                            text = "$volumePercent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Icon(
                            imageVector = ExpressiveIcons.VolumeUp,
                            contentDescription = null,
                            tint = colors.textSub,
                            modifier = Modifier.size(16.dp).clickable {
                                val newVol = (currentVolume + 1).coerceAtMost(maxVolume)
                                currentVolume = newVol
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                            }
                        )
                    }
                }
            }

            // Right Pane: Synced Karaoke Lyrics
            if (isLyricsExpanded) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(12.dp)
                ) {
                    SyncedLyricsStage(
                        lyrics = lyrics,
                        currentPlaybackMs = positionMs,
                        onSeekRequested = { targetMs -> viewModel.seekTo(targetMs) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    } else {
        // =====================================================================
        // PORTRAIT MODE (With Collapsible Lyrics & Downward Shifting)
        // =====================================================================
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar
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
                    Text(
                        text = "Sedang Diputar",
                        fontSize = 14.sp,
                        color = colors.textSub
                    )
                    Icon(
                        imageVector = ExpressiveIcons.More,
                        contentDescription = null,
                        tint = colors.textMain,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // When lyrics are closed, shift elements downward with dynamic top spacing
                if (!isLyricsExpanded) {
                    Spacer(modifier = Modifier.weight(0.35f))
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Big Album Art Cover (Dynamic Size: 170dp when lyrics open, 250dp when closed)
                val coverSize = if (isLyricsExpanded) 170.dp else 250.dp
                Box(
                    modifier = Modifier
                        .size(coverSize)
                        .clip(RoundedCornerShape(if (isLyricsExpanded) 22.dp else 30.dp))
                        .background(colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track?.thumbnailPath.isNullOrEmpty() && File(track!!.thumbnailPath).exists() && File(track!!.thumbnailPath).length() > 0) {
                        AsyncImage(
                            model = File(track!!.thumbnailPath),
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = ExpressiveIcons.Music,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(if (isLyricsExpanded) 70.dp else 96.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isLyricsExpanded) 16.dp else 22.dp))

                // Title & Artist + Collapsible Lyrics Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track?.title ?: "Memuat Lagu...",
                            fontSize = if (isLyricsExpanded) 18.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain,
                            maxLines = 1
                        )
                        Text(
                            text = track?.producer ?: "Artis",
                            fontSize = if (isLyricsExpanded) 13.sp else 15.sp,
                            color = colors.textSub
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(colors.surfaceHigh)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isUsingBackup) "MP3 FAILOVER" else "M4A",
                                fontSize = 10.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Buka / Tutup Lirik 9999px Pill Button
                        Box(
                            modifier = Modifier
                                .clip(FullPillShape)
                                .background(if (isLyricsExpanded) colors.primary else colors.surfaceHigh)
                                .clickable { isLyricsExpanded = !isLyricsExpanded }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isLyricsExpanded) ExpressiveIcons.ChevronDown else ExpressiveIcons.ChevronUp,
                                    contentDescription = null,
                                    tint = if (isLyricsExpanded) PureWhite else colors.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLyricsExpanded) "Tutup Lirik" else "Buka Lirik",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLyricsExpanded) PureWhite else colors.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isLyricsExpanded) 10.dp else 16.dp))

                // Scrubber Bar
                Slider(
                    value = if (totalDurationMs > 0) (positionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                    onValueChange = { frac ->
                        viewModel.seekTo((frac * totalDurationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.surfaceHigh
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(positionMs),
                        fontSize = 11.sp,
                        color = colors.textSub
                    )
                    Text(
                        text = formatMs(totalDurationMs),
                        fontSize = 11.sp,
                        color = colors.textSub
                    )
                }

                if (isLyricsExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Synced Karaoke Lyrics Stage (takes flexible height when open)
                    SyncedLyricsStage(
                        lyrics = lyrics,
                        currentPlaybackMs = positionMs,
                        onSeekRequested = { targetMs -> viewModel.seekTo(targetMs) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // When lyrics are closed, push the controls downward comfortably
                    Spacer(modifier = Modifier.weight(0.65f))
                }

                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ExpressiveIcons.Shuffle,
                        contentDescription = "Shuffle",
                        tint = colors.textSub,
                        modifier = Modifier.size(26.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.skipToPrev() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.Prev,
                            contentDescription = "Prev",
                            tint = colors.textMain,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Play / Pause Circle 64dp
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playbackState == EngineState.PLAYING) ExpressiveIcons.Pause else ExpressiveIcons.Play,
                            contentDescription = if (playbackState == EngineState.PLAYING) "Pause" else "Play",
                            tint = PureWhite,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.skipToNext() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.Next,
                            contentDescription = "Next",
                            tint = colors.textMain,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Icon(
                        imageVector = ExpressiveIcons.Repeat,
                        contentDescription = "Repeat",
                        tint = colors.textSub,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real Hardware Volume Control Bar (M3 Expressive 9999px Capsule)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(44.dp)
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.VolumeDown,
                            contentDescription = "Volume Turun",
                            tint = colors.textSub,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    val newVol = (currentVolume - 1).coerceAtLeast(0f)
                                    currentVolume = newVol
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                                }
                        )

                        val volumePercent = ((currentVolume / maxVolume) * 100).toInt().coerceIn(0, 100)

                        Slider(
                            value = (currentVolume / maxVolume).coerceIn(0f, 1f),
                            onValueChange = { frac ->
                                val newVol = frac * maxVolume
                                currentVolume = newVol
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primary,
                                activeTrackColor = colors.primary,
                                inactiveTrackColor = colors.surface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Text(
                            text = "$volumePercent%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Icon(
                            imageVector = ExpressiveIcons.VolumeUp,
                            contentDescription = "Volume Naik",
                            tint = colors.textSub,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    val newVol = (currentVolume + 1).coerceAtMost(maxVolume)
                                    currentVolume = newVol
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
