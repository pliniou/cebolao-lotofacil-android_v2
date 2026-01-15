package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimen {
    // --- Modern Spacing System (8pt Grid) ---
    val SpacingTiny = 2.dp
    val Spacing4 = 4.dp
    val Spacing8 = 8.dp
    val Spacing12 = 12.dp
    val Spacing16 = 16.dp
    val Spacing20 = 20.dp
    val Spacing24 = 24.dp

    // --- Semantic Spacing (Compactado para melhor aproveitamento) ---
    val SpacingShort = Spacing4
    val SpacingMedium = Spacing12
    val SectionSpacing = Spacing16
    val ItemSpacing = Spacing8
    val TrackingWidest = 2.sp

    // --- Layout (Compactado) ---
    val ScreenPadding = Spacing12
    val CardContentPadding = Spacing16
    val BottomContentPadding = 112.dp

    // --- Component Sizes ---
    val ActionButtonHeight = 48.dp
    val ActionButtonHeightLarge = 56.dp
    val SmallButtonHeight = 36.dp

    // --- Indicators ---
    val IndicatorHeightSmall = 8.dp
    val IndicatorWidthActive = 32.dp

    // --- Icons ---
    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val Logo = 80.dp

    // --- Lotofácil Balls (Compactado) ---
    val BallSizeLarge = 40.dp
    val BallSizeMedium = 28.dp 
    val BallSizeSmall = 20.dp
    val BallSpacing = 4.dp
    val BallTextLarge = 16.sp
    val BallTextMedium = 12.sp
    val BallTextSmall = 9.sp
    val BarChartHeight = 160.dp

    // --- Shapes ---
    val CardCornerRadius = 24.dp

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Medium = 1.5.dp
        val Thick = 2.dp
    }
    
    object Elevation {
        val None = 0.dp
        val Level2 = 2.dp
    }
}
