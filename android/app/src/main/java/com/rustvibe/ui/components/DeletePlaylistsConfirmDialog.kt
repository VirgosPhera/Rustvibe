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
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite

@Composable
fun DeletePlaylistsConfirmDialog(
    selectedPlaylists: List<PlaylistEntity>,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val badgeErrorBg = Color(0xFF52170B)
    val badgeErrorFg = Color(0xFFFF897D)
    val badgeSuccessFg = Color(0xFF78DC8B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC180F10))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.72f else 0.90f)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .clickable(enabled = false) {}
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trash Hero Icon Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(badgeErrorBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ExpressiveIcons.Trash,
                        contentDescription = "Hapus",
                        tint = badgeErrorFg,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hapus ${selectedPlaylists.size} Playlist?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )

                Text(
                    text = "Playlist berikut akan dihapus dari daftar putar:",
                    fontSize = 12.sp,
                    color = colors.textSub,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Playlist Names Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surfaceHigh)
                        .padding(14.dp)
                ) {
                    Column {
                        selectedPlaylists.take(5).forEach { pl ->
                            Text(
                                text = "• ${pl.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                        if (selectedPlaylists.size > 5) {
                            Text(
                                text = "+ ${selectedPlaylists.size - 5} playlist lainnya...",
                                fontSize = 11.sp,
                                color = colors.textSub
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✓ File audio fisik di memori HP TIDAK akan ikut terhapus.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeSuccessFg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: FullPillShape 9999px
                // Button 1: Hapus Sekarang (Destructive Crimson Pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(FullPillShape)
                        .background(colors.primary)
                        .clickable(onClick = onConfirmDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ExpressiveIcons.Trash,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hapus ${selectedPlaylists.size} Playlist Sekarang",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Button 2: Batal (Surface High Pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Batal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                }
            }
        }
    }
}
