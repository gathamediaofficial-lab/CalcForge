package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val CalcForgeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 46.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.0).sp,
        fontFeatureSettings = "tnum"
    ),
    displayMedium = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    ),
    displaySmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = "tnum"
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
        fontFeatureSettings = "tnum"
    )
)
