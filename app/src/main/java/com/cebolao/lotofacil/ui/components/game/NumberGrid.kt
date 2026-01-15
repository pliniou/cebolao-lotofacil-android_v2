package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.unit.dp

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
    )
) {
    val isFull = maxSelection != null && selectedNumbers.size >= maxSelection

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
        maxItemsInEachRow = GameConstants.GRID_COLUMNS
    ) {
        allNumbers.forEach { number ->
            key(number) {
                val isSelected = number in selectedNumbers
                val clickable = !isFull || isSelected

                val heatmapColor = heatmapColors?.get(number)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
                        .clip(CircleShape)
                        .clickable(enabled = clickable) { onNumberClick(number) }
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
        }
    }
}