package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CalcForgeDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberContainerDark,
    onPrimaryContainer = AmberPrimaryLight,
    secondary = CyanSecondary,
    onSecondary = Color(0xFF052028),
    secondaryContainer = CyanContainerDark,
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = EmeraldAccent,
    onTertiary = Color(0xFF042218),
    background = ObsidianBackground,
    onBackground = InkPrimaryDark,
    surface = ObsidianSurface,
    onSurface = InkPrimaryDark,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = InkSecondaryDark,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianSurfaceVariant,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF1F2736),
    error = RoseDanger,
    onError = Color.White,
    errorContainer = RoseContainerDark,
    onErrorContainer = Color(0xFFFFB3C4)
)

private val CalcForgeLightColorScheme = lightColorScheme(
    primary = AmberPrimaryOnLight,
    onPrimary = Color.White,
    primaryContainer = AmberContainerLight,
    onPrimaryContainer = Color(0xFF7A2404),
    secondary = CyanSecondaryOnLight,
    onSecondary = Color.White,
    secondaryContainer = CyanContainerLight,
    onSecondaryContainer = Color(0xFF054154),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = CeramicBackground,
    onBackground = InkPrimaryLight,
    surface = CeramicSurface,
    onSurface = InkPrimaryLight,
    surfaceVariant = CeramicSurfaceVariant,
    onSurfaceVariant = InkSecondaryLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = CeramicBackground,
    surfaceContainer = CeramicSurfaceVariant,
    surfaceContainerHigh = CeramicSurfaceHigh,
    outline = CeramicBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = RoseDangerOnLight,
    onError = Color.White,
    errorContainer = RoseContainerLight,
    onErrorContainer = Color(0xFF881337)
)

val CalcForgeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CalcForgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CalcForgeDarkColorScheme else CalcForgeLightColorScheme
    val calcColors = if (darkTheme) DarkCalcColors else LightCalcColors

    CompositionLocalProvider(LocalCalcColors provides calcColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CalcForgeTypography,
            shapes = CalcForgeShapes,
            content = content
        )
    }
}
