package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Grid of number balls for lottery number selection
 */
@Composable
fun NumberGrid(
    selectedNumbers: Set<Int>,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxSelection: Int? = null,
    onItemRecompose: ((Int, Int) -> Unit)? = null
) {
    val numbers = (1..25).toList()
    val chunkedNumbers = numbers.chunked(5)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedNumbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { number ->
                    val isSelected = number in selectedNumbers
                    val canClick = maxSelection == null || isSelected || selectedNumbers.size < maxSelection

                    NumberBall(
                        number = number,
                        isSelected = isSelected,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        onClick = if (canClick) {{ onNumberClick(number) }} else null
                    )

                    onItemRecompose?.invoke(number, 1)
                }
            }
        }
    }
}
