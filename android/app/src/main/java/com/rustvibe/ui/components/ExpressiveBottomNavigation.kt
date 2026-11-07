package com.rustvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustvibe.ui.theme.AppTheme
import com.rustvibe.ui.theme.BottomNavShape
import com.rustvibe.ui.theme.FullPillShape
import com.rustvibe.ui.theme.PureWhite

enum class NavTab(val label: String, val icon: ImageVector) {
    BERANDA("Beranda", ExpressiveIcons.Home),
    CARI("Cari", ExpressiveIcons.Search),
    PLAYLIST("Playlist", ExpressiveIcons.Playlist),
    PROFIL("Profil", ExpressiveIcons.Profile)
}

@Composable
fun ExpressiveBottomNavigation(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompact = maxWidth < 340.dp
        val isVeryCompact = maxWidth < 260.dp
        val barHeight = if (isCompact) 58.dp else 72.dp
        val iconSize = if (isCompact) 20.dp else 24.dp
        val pillHeight = if (isCompact) 28.dp else 34.dp
        val pillWidth = if (isVeryCompact) 42.dp else if (isCompact) 52.dp else 64.dp
        val labelSize = if (isCompact) 9.sp else 11.sp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(BottomNavShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompact) 4.dp else 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(FullPillShape)
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = if (isCompact) 2.dp else 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(width = pillWidth, height = pillHeight)
                                    .clip(FullPillShape)
                                    .background(colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = PureWhite,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                            if (!isVeryCompact) {
                                Text(
                                    text = tab.label,
                                    fontSize = labelSize,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(width = pillWidth, height = pillHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = colors.textSub,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                            if (!isVeryCompact) {
                                Text(
                                    text = tab.label,
                                    fontSize = labelSize,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
