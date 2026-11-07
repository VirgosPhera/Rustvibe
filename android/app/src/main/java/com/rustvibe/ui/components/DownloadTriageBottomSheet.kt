package com.rustvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.BottomSheetShape
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite

@Composable
fun DownloadTriageBottomSheet(
    onSaveToPlaylistClick: () -> Unit,
    onEphemeralListenClick: () -> Unit,
    onCancelClick: () -> Unit,
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

            Spacer(modifier = Modifier.height(20.dp))

            // Checkmark Circle Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(colors.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ExpressiveIcons.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct & To-the-Point Title and Subtitle
            Text(
                text = "Lagu Berhasil Diunduh",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pilih tindakan penyimpanan:",
                fontSize = 13.sp,
                color = colors.textSub
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Option A: Simpan ke Playlist (Primary CTA 9999px)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(FullPillShape)
                    .background(colors.primary)
                    .clickable(onClick = onSaveToPlaylistClick),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ExpressiveIcons.Playlist,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan ke Playlist",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option B: Dengerin Sekali Saja (Secondary Capsule 9999px)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(FullPillShape)
                    .background(colors.surfaceHigh)
                    .clickable(onClick = onEphemeralListenClick),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ExpressiveIcons.Bolt,
                        contentDescription = null,
                        tint = colors.textSub,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dengerin Sekali (Lalu Hapus)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSub
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Button: Batal
            Box(
                modifier = Modifier
                    .clip(FullPillShape)
                    .clickable(onClick = onCancelClick)
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
