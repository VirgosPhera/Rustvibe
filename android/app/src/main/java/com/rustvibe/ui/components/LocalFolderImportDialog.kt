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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.data.local.ScannedFolderResult
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite
import com.rustvibe.ui.theme.TrackCardShape

@Composable
fun LocalFolderImportDialog(
    scannedFolder: ScannedFolderResult,
    onConfirm: (playlistName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var playlistName by remember { mutableStateOf(scannedFolder.folderName) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC1A1012))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.85f else 0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .clickable(enabled = false) {}
                .padding(if (isLandscape) 14.dp else 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isLandscape) 38.dp else 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.FolderOpen,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Import Folder Musik Lokal",
                            fontSize = if (isLandscape) 15.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                        Text(
                            text = "${scannedFolder.tracks.size} Lagu Audio Terdeteksi",
                            fontSize = 11.sp,
                            color = colors.textSub
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 16.dp))

                Text(
                    text = "Nama Playlist di RustVibe:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSub
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Editable Playlist Name Text Field (9999px Full Pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isLandscape) 42.dp else 48.dp)
                        .clip(FullPillShape)
                        .background(colors.surfaceHigh)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            textStyle = TextStyle(
                                fontSize = if (isLandscape) 13.sp else 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textMain
                            ),
                            cursorBrush = SolidColor(colors.primary),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = ExpressiveIcons.Bolt,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Track Preview Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceHigh)
                        .padding(10.dp)
                ) {
                    Column {
                        val previewText = scannedFolder.tracks.take(4).joinToString(", ") { it.title }
                        val remaining = (scannedFolder.tracks.size - 4).coerceAtLeast(0)
                        Text(
                            text = if (remaining > 0) "Daftar: $previewText + $remaining lainnya" else "Daftar: $previewText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Audio & cover ID3 akan disalin aman ke penyimpanan lokal RustVibe",
                            fontSize = 10.sp,
                            color = colors.textSub
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons (9999px Full Pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(FullPillShape)
                            .background(colors.surfaceHigh)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSub
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(42.dp)
                            .clip(FullPillShape)
                            .background(colors.primary)
                            .clickable { onConfirm(playlistName) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mulai Import (${scannedFolder.tracks.size} Lagu)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}
