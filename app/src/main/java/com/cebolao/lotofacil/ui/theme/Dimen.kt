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
    val Spacing32 = 32.dp

    val SpacingShort = Spacing8
    val SpacingMedium = Spacing16
    
    val TrackingWidest = 2.sp

    // --- Aliases for Legacy Compatibility ---
    val SpacingXS = Spacing4
    val SpacingS = Spacing8
    val ExtraSmallPadding = SpacingXS
    val SmallPadding = SpacingS

    val SectionSpacing = Spacing24
    val ItemSpacing = Spacing12

    // --- Layout ---
    val ScreenPadding = Spacing16
    val CardContentPadding = Spacing20
    val BottomContentPadding = 112.dp

    // --- Component Sizes ---
    val ActionButtonHeight = 48.dp
    val ActionButtonHeightLarge = 56.dp
    val SmallButtonHeight = 36.dp

    // --- Indicators ---
    val IndicatorHeightSmall = 8.dp
    val IndicatorWidthActive = 32.dp
    val IndicatorWidthInactive = 8.dp

    // --- Icons ---
    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val IconExtraLarge = 48.dp

    // Legacy Aliases
    val SmallIcon = IconSmall
    val MediumIcon = IconMedium
    val LargeIcon = IconLarge
    val ExtraLargeIcon = IconExtraLarge
    val Logo = 80.dp

    // --- Lotofácil Balls ---
    val BallSizeLarge = 48.dp
    val BallSizeMedium = 32.dp 
    val BallSizeSmall = 24.dp
    val BallSpacing = 6.dp
    val BallTextLarge = 18.sp
    val BallTextMedium = 14.sp
    val BallTextSmall = 10.sp
    val BarChartHeight = 180.dp

    // --- Shapes ---
    val CardCornerRadius = 24.dp
    val ButtonCornerRadius = 16.dp

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Medium = 1.5.dp
        val Thick = 2.dp
    }
    
    object Elevation {
        val None = 0.dp
        val Level1 = 1.dp
        val Level2 = 2.dp
        val Level3 = 4.dp
        val Level4 = 8.dp
    }
}
