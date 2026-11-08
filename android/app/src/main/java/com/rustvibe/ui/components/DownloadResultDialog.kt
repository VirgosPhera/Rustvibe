package com.rustvibe.ui.components

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.data.remote.DownloadResultDialogState
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite

@Composable
fun DownloadResultDialog(
    state: DownloadResultDialogState,
    onRetry: () -> Unit,
    onOpenPlaylist: (playlistId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val badgeSuccessBg = Color(0xFF1B3B22)
    val badgeSuccessFg = Color(0xFF78DC8B)
    val badgeErrorBg = Color(0xFF52170B)
    val badgeErrorFg = Color(0xFFFF897D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC180F10))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.85f else 0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (state.failedCount == 0) badgeSuccessBg else colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.failedCount == 0) ExpressiveIcons.Check else ExpressiveIcons.Check,
                        contentDescription = "Status",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hasil Unduhan Playlist",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )

                Text(
                    text = "Playlist '${state.playlistName}'",
                    fontSize = 13.sp,
                    color = colors.textSub,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Honest Statistics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Success Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surfaceHigh)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(badgeSuccessBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Check,
                                        contentDescription = null,
                                        tint = badgeSuccessFg,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${state.successCount} Lagu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tersimpan di Disk",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeSuccessFg
                            )
                        }
                    }

                    // Failed Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surfaceHigh)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(badgeErrorBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ExpressiveIcons.Warning,
                                        contentDescription = null,
                                        tint = badgeErrorFg,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${state.failedCount} Lagu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (state.failedCount > 0) "Gagal Diunduh" else "Semua Sukses",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.failedCount > 0) badgeErrorFg else badgeSuccessFg
                            )
                        }
                    }
                }

                // If there are failed songs, show the detail banner
                if (state.failedCount > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF523639))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "${state.failedCount} Lagu Belum Berhasil Masuk:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeErrorFg
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            state.failedSongs.take(4).forEach { song ->
                                Text(
                                    text = "• ${song.title}",
                                    fontSize = 11.sp,
                                    color = PureWhite,
                                    maxLines = 1
                                )
                            }
                            if (state.failedSongs.size > 4) {
                                Text(
                                    text = "+ ${state.failedSongs.size - 4} lagu lainnya...",
                                    fontSize = 10.sp,
                                    color = colors.textSub
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Koneksi YouTube terputus atau format stream ditolak. Tekan coba lagi untuk mengulang unduhan khusus lagu ini.",
                                fontSize = 10.sp,
                                color = colors.textSub
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (FullPillShape 9999px)
                if (state.failedCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(FullPillShape)
                            .background(colors.primary)
                            .clickable(onClick = onRetry),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ExpressiveIcons.Refresh,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coba Lagi ${state.failedCount} Lagu yang Gagal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (state.successCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(FullPillShape)
                            .background(colors.surfaceHigh)
                            .clickable { onOpenPlaylist(state.playlistId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ExpressiveIcons.Playlist,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka Playlist (${state.successCount} Lagu)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .clip(FullPillShape)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tutup Dialog",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSub
                    )
                }
            }
        }
    }
}
