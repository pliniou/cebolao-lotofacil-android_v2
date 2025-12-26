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
    val Spacing24 = 24.dp

    val SpacingShort = Spacing8
    val SpacingMedium = Spacing16

    // --- Aliases for Legacy Compatibility ---
    val SpacingXS = Spacing4
    val SpacingS = Spacing8
    val SpacingL = Spacing24
    val ExtraSmallPadding = SpacingXS
    val SmallPadding = SpacingS
    val LargePadding = SpacingL

    val SectionSpacing = Spacing24
    val ItemSpacing = SpacingShort

    // --- Layout ---
    val ScreenPadding = SpacingMedium
    val CardContentPadding = SpacingMedium
    val BottomContentPadding = 96.dp

    // --- Component Sizes ---
    val ActionButtonHeight = 48.dp
    val SmallButtonHeight = 32.dp

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
    val BallSpacing = 4.dp
    val BallTextLarge = 18.sp
    val BallTextMedium = 14.sp
    val BallTextSmall = 10.sp
    val CheckResultChartHeight = 160.dp
    val BarChartHeight = 180.dp

    object Glass {
        val BorderWidth = 0.5.dp
    }

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
    }
    
    object Elevation {
        val None = 0.dp
        val Level1 = 1.dp
        val Level2 = 3.dp
        val Low = Level1
        val Medium = Level2
    }
}
