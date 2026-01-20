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

    // --- Semantic Spacing ---
    val SpacingShort = Spacing4
    val SpacingMedium = Spacing12
    val SectionSpacing = Spacing24 // Aumentado para melhor separação visual (flat design)
    val ItemSpacing = Spacing8
    val TrackingWidest = 2.sp
    val ElevationMedium = 0.dp // Reduzido para 0dp para garantir visual "flat"

    // --- Layout ---
    val ScreenPadding = Spacing16
    val CardContentPadding = Spacing16
    val BottomContentPadding = 112.dp
    val LayoutMaxWidth = 900.dp
    val CardMaxWidth = 520.dp

    // --- Component Sizes ---
    val ActionButtonHeight = 48.dp
    val ActionButtonHeightLarge = 56.dp
    val SmallButtonHeight = 36.dp
    val ControlHeightMedium = 40.dp
    val ControlWidthMedium = 60.dp
    val TableColumnWidthMedium = 70.dp
    val TableColumnWidthLarge = 80.dp
    val TableColumnWidthXLarge = 100.dp
    val LoadingCardHeight = 120.dp
    val GameCardMinHeight = 250.dp
    val ChartHeightSmall = 140.dp
    val ChartHeightMini = 100.dp
    val MiniBarWidth = 40.dp
    val MiniBarHeight = 6.dp

    // --- Indicators ---
    val IndicatorHeightSmall = 8.dp
    val IndicatorWidthActive = 32.dp

    // --- Icons ---
    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val LoadingIndicatorSize = 36.dp
    val Logo = 80.dp
    val IconTiny = 18.dp

    // --- Lotofácil Balls ---
    val BallSizeLarge = 40.dp
    val BallSizeMedium = 28.dp 
    val BallSizeSmall = 20.dp
    val BallTouchSizeLarge = 44.dp
    val BallTouchSizeMedium = 32.dp
    val BallTouchSizeSmall = 26.dp
    val BallSpacing = 4.dp
    val BallTextLarge = 16.sp
    val BallTextMedium = 12.sp
    val BallTextSmall = 9.sp
    val BarChartHeight = 160.dp
    val ChartPreviewHeight = 200.dp
    val ChartAxisLabelPadding = 40.dp
    val ChartLineStroke = 3.dp
    val ChartDotRadiusSmall = 3.dp
    val ChartDotRadiusLarge = 5.dp

    // --- Shapes (Flat Design: preferring larger, softer corners) ---
    val CardCornerRadius = 28.dp
    val CornerRadiusSmall = 16.dp
    val CornerRadiusMedium = 20.dp
    val CornerRadiusTiny = 4.dp

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Medium = 1.5.dp
        val Thick = 2.dp
    }
    
    object Elevation {
        val None = 0.dp
        val Level1 = 0.dp // Leveling everything to 0dp for flat design
        val Level2 = 0.dp
        val Level3 = 0.dp
        val Level4 = 0.dp
        val Level5 = 0.dp
    }
    
    object TouchTarget {
        val Minimum = 48.dp
        val Comfortable = 56.dp
        val Action = 64.dp
    }
    
    object Ripple {
        val Small = 20.dp
        val Medium = 28.dp
        val Large = 36.dp
    }
}
