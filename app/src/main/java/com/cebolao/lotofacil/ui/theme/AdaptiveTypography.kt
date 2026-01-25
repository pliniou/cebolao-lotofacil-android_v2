package com.cebolao.lotofacil.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun rememberTypographyScale(): Float {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthDp = with(density) { windowInfo.containerSize.width.toDp().value }

    return remember(widthDp) {
        // Use 360dp as reference width (standard phone)
        (widthDp / 360f).coerceIn(0.85f, 1.3f)
    }
}

fun scaleSp(baseSize: TextUnit, scale: Float): TextUnit = (baseSize.value * scale).sp
