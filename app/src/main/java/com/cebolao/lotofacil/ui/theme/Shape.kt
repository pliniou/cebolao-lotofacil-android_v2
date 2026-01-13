package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(Dimen.Spacing4),
    small = RoundedCornerShape(Dimen.Spacing8),
    medium = RoundedCornerShape(Dimen.Spacing12),
    large = RoundedCornerShape(Dimen.CardCornerRadius),
    extraLarge = RoundedCornerShape(Dimen.CardCornerRadius * 1.5f)
)
