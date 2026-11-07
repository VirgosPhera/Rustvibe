package com.rustvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.BottomSheetShape
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.TrackCardShape

@Composable
fun PlaylistPickerBottomSheet(
    playlists: List<PlaylistEntity>,
    onSelectPlaylist: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(BottomSheetShape)
            .background(colors.surface)
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(FullPillShape)
                    .background(colors.surfaceHighest)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Pilih Playlist Tujuan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Lagu akan otomatis ditaruh di urutan paling belakang",
                fontSize = 12.sp,
                color = colors.textSub
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists) { playlist ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(TrackCardShape)
                            .background(colors.surfaceHigh)
                            .clickable { onSelectPlaylist(playlist.id) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = ExpressiveIcons.Playlist,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = playlist.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Pilih",
                                fontSize = 12.sp,
                                color = colors.textSub
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button
            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .clickable(onClick = onCancel)
                .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Batal",
                    fontSize = 14.sp,
                    color = colors.textSub
                )
            }
        }
    }
}
