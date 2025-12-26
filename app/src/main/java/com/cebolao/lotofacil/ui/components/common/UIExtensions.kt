package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Modifier que adiciona scale animation para estado selecionado.
 */
fun Modifier.selectedScale(isSelected: Boolean): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) Motion.Offset.SELECTSCALE else 1f,
        animationSpec = Motion.Spring.snappy(),
        label = "selectedScale"
    )
    this.scale(scale)
}
