package com.rustvibe.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode(val title: String, val subtitle: String) {
    DARK("Tema Gelap", "Coffee Bean • Deep Crimson"),
    LIGHT("Tema Terang", "Antique White • Crimson Carrot"),
    DYNAMIC_SYSTEM("Aksen Sistem Android 15-16", "Monet Dynamic Wallpaper Palette")
}

data class ExpressiveThemeColors(
    val bg: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val primary: Color,
    val secondary: Color,
    val textMain: Color,
    val textSub: Color,
    val accentContainer: Color,
    val lyricBg: Color,
    val lyricText: Color,
    val isDark: Boolean
)

val DarkThemeColors = ExpressiveThemeColors(
    bg = CoffeeBean,
    surface = SurfaceContainer,
    surfaceHigh = SurfaceContainerHigh,
    surfaceHighest = SurfaceContainerHighest,
    primary = DeepCrimsonCarrot,
    secondary = HotFuchsia,
    textMain = PureWhite,
    textSub = CottonRose,
    accentContainer = DarkAccent,
    lyricBg = ActiveLyricContainer,
    lyricText = ActiveLyricText,
    isDark = true
)

val LightThemeColors = ExpressiveThemeColors(
    bg = AntiqueWhite,
    surface = SurfaceContainerLight,
    surfaceHigh = SurfaceContainerHighLight,
    surfaceHighest = SurfaceContainerHighestLight,
    primary = VibrantCrimsonCarrot,
    secondary = HotFuchsia,
    textMain = CoffeeBean,
    textSub = TextMutedLight,
    accentContainer = ActiveLyricContainerLight,
    lyricBg = ActiveLyricContainerLight,
    lyricText = ActiveLyricTextLight,
    isDark = false
)

val LocalExpressiveColors = staticCompositionLocalOf { DarkThemeColors }

object AppTheme {
    val colors: ExpressiveThemeColors
        @Composable
        get() = LocalExpressiveColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = DeepCrimsonCarrot,
    onPrimary = PureWhite,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = AntiqueWhite,
    secondary = HotFuchsia,
    onSecondary = PureWhite,
    secondaryContainer = ActiveLyricContainer,
    onSecondaryContainer = ActiveLyricText,
    tertiary = AntiqueWhite,
    onTertiary = CoffeeBean,
    background = CoffeeBean,
    onBackground = PureWhite,
    surface = CoffeeBean,
    onSurface = PureWhite,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = AntiqueWhite,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantCrimsonCarrot,
    onPrimary = PureWhite,
    primaryContainer = ActiveLyricContainerLight,
    onPrimaryContainer = ActiveLyricTextLight,
    secondary = HotFuchsia,
    onSecondary = PureWhite,
    secondaryContainer = ActiveLyricContainerLight,
    onSecondaryContainer = ActiveLyricTextLight,
    tertiary = VibrantCrimsonCarrot,
    onTertiary = PureWhite,
    background = AntiqueWhite,
    onBackground = CoffeeBean,
    surface = AntiqueWhite,
    onSurface = CoffeeBean,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = TextMutedLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight
)

fun createDynamicColors(context: Context, isDark: Boolean): ExpressiveThemeColors {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val scheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        return ExpressiveThemeColors(
            bg = scheme.background,
            surface = scheme.surfaceContainer,
            surfaceHigh = scheme.surfaceContainerHigh,
            surfaceHighest = scheme.surfaceContainerHighest,
            primary = scheme.primary,
            secondary = scheme.tertiary,
            textMain = scheme.onBackground,
            textSub = scheme.onSurfaceVariant,
            accentContainer = scheme.primaryContainer,
            lyricBg = scheme.secondaryContainer,
            lyricText = scheme.onSecondaryContainer,
            isDark = isDark
        )
    }
    return if (isDark) DarkThemeColors else LightThemeColors
}

@Composable
fun RustVibeTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    darkTheme: Boolean = (themeMode != ThemeMode.LIGHT),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val (colorScheme, expressiveColors) = when (themeMode) {
        ThemeMode.DYNAMIC_SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val scheme = if (systemInDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                Pair(scheme, createDynamicColors(context, systemInDark))
            } else {
                Pair(DarkColorScheme, DarkThemeColors)
            }
        }
        ThemeMode.LIGHT -> Pair(LightColorScheme, LightThemeColors)
        ThemeMode.DARK -> Pair(DarkColorScheme, DarkThemeColors)
    }

    CompositionLocalProvider(LocalExpressiveColors provides expressiveColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = RustVibeShapes,
            content = content
        )
    }
}
