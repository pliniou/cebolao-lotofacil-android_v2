package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeIndicatorWidth: Dp = Dimen.IndicatorWidthActive,
    indicatorHeight: Dp = Dimen.IndicatorHeightSmall,
    indicatorSpacing: Dp = Dimen.Spacing16
) {
    Row(
        modifier = modifier.semantics {
            val current = (currentPage + 1).coerceIn(1, pageCount.coerceAtLeast(1))
            contentDescription = "Página $current de $pageCount"
            stateDescription = "Página $current de $pageCount"
        },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isSelected = currentPage == iteration
            val width by animateDpAsState(
                targetValue = if (isSelected) activeIndicatorWidth else indicatorHeight,
                label = "indicatorWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                label = "indicatorColor"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = indicatorSpacing / 2)
                    .height(indicatorHeight)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
