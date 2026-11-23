package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Precision Obsidian Dark Palette ---
val ObsidianBackground = Color(0xFF0C0E12)
val ObsidianSurface = Color(0xFF131720)
val ObsidianSurfaceVariant = Color(0xFF1A202C)
val ObsidianSurfaceHigh = Color(0xFF232B3A)
val ObsidianBorder = Color(0xFF2B3446)

val AmberPrimary = Color(0xFFFF6B2B)
val AmberPrimaryLight = Color(0xFFFF8A57)
val AmberContainerDark = Color(0xFF3E1C0E)
val CyanSecondary = Color(0xFF22D3EE)
val CyanContainerDark = Color(0xFF0E2B35)
val RoseDanger = Color(0xFFFF5277)
val RoseContainerDark = Color(0xFF3A1520)
val EmeraldAccent = Color(0xFF10B981)

val InkPrimaryDark = Color(0xFFF3F6FA)
val InkSecondaryDark = Color(0xFF94A3B8)
val InkTertiaryDark = Color(0xFF64748B)

// --- Matte Ceramic Light Palette ---
val CeramicBackground = Color(0xFFF3F5F8)
val CeramicSurface = Color(0xFFFFFFFF)
val CeramicSurfaceVariant = Color(0xFFE8EDF4)
val CeramicSurfaceHigh = Color(0xFFDCE3EE)
val CeramicBorder = Color(0xFFCFD8E6)

val AmberPrimaryOnLight = Color(0xFFE8561A)
val AmberContainerLight = Color(0xFFFFE5D9)
val CyanSecondaryOnLight = Color(0xFF0284A8)
val CyanContainerLight = Color(0xFFD9F5FC)
val RoseDangerOnLight = Color(0xFFD92D56)
val RoseContainerLight = Color(0xFFFFE1E8)

val InkPrimaryLight = Color(0xFF0F172A)
val InkSecondaryLight = Color(0xFF475569)
val InkTertiaryLight = Color(0xFF64748B)

@Immutable
data class CalcColors(
    val displayBackground: Color,
    val displayBorder: Color,
    val displayGlowPrimary: Color,
    val displayGlowSecondary: Color,
    val numKeyContainer: Color,
    val numKeyContent: Color,
    val numKeyBorder: Color,
    val opKeyContainer: Color,
    val opKeyContent: Color,
    val sciKeyContainer: Color,
    val sciKeyContent: Color,
    val sciKeyActiveContainer: Color,
    val sciKeyActiveContent: Color,
    val clearKeyContainer: Color,
    val clearKeyContent: Color,
    val equalsKeyContainer: Color,
    val equalsKeyContent: Color,
    val memChipContainer: Color,
    val memChipContent: Color,
    val positiveAccent: Color,
    val subtleDivider: Color
)

val DarkCalcColors = CalcColors(
    displayBackground = Color(0xFF11151E),
    displayBorder = Color(0xFF252E3F),
    displayGlowPrimary = Color(0x22FF6B2B),
    displayGlowSecondary = Color(0x1E22D3EE),
    numKeyContainer = Color(0xFF1A202C),
    numKeyContent = Color(0xFFF3F6FA),
    numKeyBorder = Color(0xFF283142),
    opKeyContainer = Color(0xFF232C3D),
    opKeyContent = Color(0xFFFF8A57),
    sciKeyContainer = Color(0xFF11212B),
    sciKeyContent = Color(0xFF67E8F9),
    sciKeyActiveContainer = Color(0xFF164E63),
    sciKeyActiveContent = Color(0xFFECFEFF),
    clearKeyContainer = Color(0xFF33151F),
    clearKeyContent = Color(0xFFFF6B8A),
    equalsKeyContainer = Color(0xFFFF6B2B),
    equalsKeyContent = Color(0xFFFFFFFF),
    memChipContainer = Color(0xFF171D28),
    memChipContent = Color(0xFF94A3B8),
    positiveAccent = EmeraldAccent,
    subtleDivider = Color(0xFF1F2736)
)

val LightCalcColors = CalcColors(
    displayBackground = Color(0xFFFFFFFF),
    displayBorder = Color(0xFFD5DFEC),
    displayGlowPrimary = Color(0x18E8561A),
    displayGlowSecondary = Color(0x160284A8),
    numKeyContainer = Color(0xFFFFFFFF),
    numKeyContent = Color(0xFF0F172A),
    numKeyBorder = Color(0xFFDCE4EF),
    opKeyContainer = Color(0xFFFFEDE5),
    opKeyContent = Color(0xFFD94A11),
    sciKeyContainer = Color(0xFFE3F5FA),
    sciKeyContent = Color(0xFF036988),
    sciKeyActiveContainer = Color(0xFF0284A8),
    sciKeyActiveContent = Color(0xFFFFFFFF),
    clearKeyContainer = Color(0xFFFFE4EA),
    clearKeyContent = Color(0xFFD92D56),
    equalsKeyContainer = Color(0xFFE8561A),
    equalsKeyContent = Color(0xFFFFFFFF),
    memChipContainer = Color(0xFFEAF0F7),
    memChipContent = Color(0xFF475569),
    positiveAccent = Color(0xFF059669),
    subtleDivider = Color(0xFFE2E8F0)
)

val LocalCalcColors = staticCompositionLocalOf { DarkCalcColors }
