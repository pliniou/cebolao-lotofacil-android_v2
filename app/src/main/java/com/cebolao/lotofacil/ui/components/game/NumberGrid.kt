package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.defaultMinSize

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val ALL_NUMBERS: ImmutableList<Int> =
    (GameConstants.MIN_NUMBER..GameConstants.MAX_NUMBER).toList().toImmutableList()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberGrid(
    selectedNumbers: Set<Int>,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    allNumbers: ImmutableList<Int> = ALL_NUMBERS,
    maxSelection: Int? = null,
    sizeVariant: NumberBallSize = NumberBallSize.Medium,
    ballVariant: NumberBallVariant = NumberBallVariant.Primary,
    heatmapColors: Map<Int, androidx.compose.ui.graphics.Color>? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        Dimen.BallSpacing,
        Alignment.CenterHorizontally
    ),
    // Optional debug callback for tests/profiling. Null in production.
    onItemRecompose: ((number: Int, count: Int) -> Unit)? = null
) {
    val isFull = remember(maxSelection, selectedNumbers.size) {
        derivedStateOf { maxSelection != null && selectedNumbers.size >= maxSelection }
    }.value
    val minSize = remember(sizeVariant) {
        when (sizeVariant) {
            NumberBallSize.Large -> Dimen.BallTouchSizeLarge
            NumberBallSize.Medium -> Dimen.BallTouchSizeMedium
            NumberBallSize.Small -> Dimen.BallTouchSizeSmall
        }
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
        maxItemsInEachRow = GameConstants.GRID_COLUMNS
    ) {
        val currentOnNumberClick = rememberUpdatedState(onNumberClick)
        allNumbers.forEach { number ->
            key(number) {
                val isSelected = number in selectedNumbers
                val clickable = !isFull || isSelected
                val heatmapColor = remember(number, heatmapColors) { heatmapColors?.get(number) }

                NumberGridItem(
                    number = number,
                    isSelected = isSelected,
                    clickable = clickable,
                    onNumberClick = { currentOnNumberClick.value(it) },
                    minSize = minSize,
                    sizeVariant = sizeVariant,
                    ballVariant = ballVariant,
                    heatmapColor = heatmapColor,
                    onRecompose = onItemRecompose
                )
            }
        }
    }
}

@Composable
private fun NumberGridItem(
    number: Int,
    isSelected: Boolean,
    clickable: Boolean,
    onNumberClick: (Int) -> Unit,
    minSize: androidx.compose.ui.unit.Dp,
    sizeVariant: NumberBallSize,
    ballVariant: NumberBallVariant,
    heatmapColor: androidx.compose.ui.graphics.Color?,
    onRecompose: ((number: Int, count: Int) -> Unit)? = null
) {
    val boxBaseModifier = remember(minSize) {
        Modifier
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .clip(CircleShape)
    }

    val boxModifier = remember(boxBaseModifier, clickable) {
        if (clickable) boxBaseModifier.clickable { onNumberClick(number) } else boxBaseModifier
    }

    // Debug hook: count recompositions for profiling tests. SideEffect runs after each successful
    // composition/recomposition, so it provides a simple counter for how often an item is recomposed.
    val recomposeCount = remember { androidx.compose.runtime.mutableStateOf(0) }
    SideEffect {
        recomposeCount.value = recomposeCount.value + 1
        onRecompose?.invoke(number, recomposeCount.value)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = boxModifier
    ) {
        NumberBall(
            number = number,
            modifier = Modifier,
            sizeVariant = sizeVariant,
            isSelected = isSelected,
            isDisabled = !clickable,
            variant = ballVariant,
            customBackgroundColor = heatmapColor
        )
    }
}

