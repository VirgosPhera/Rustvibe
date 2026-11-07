package com.rustvibe.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RustVibeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),         // Track thumbnails & cover cards
    large = RoundedCornerShape(24.dp),          // Bottom sheet top corners & Full player covers
    extraLarge = RoundedCornerShape(32.dp)      // Modal bottom sheets & large cards
)

// Full 9999px Capsule Shapes for buttons, active pills, search bar, and floating player
val FullPillShape = RoundedCornerShape(9999.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
val BottomNavShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
val TrackCardShape = RoundedCornerShape(16.dp)
val GridCoverShape = RoundedCornerShape(12.dp)
