package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.domain.model.UiLotofacilGame

/**
 * Card component for displaying and interacting with a game
 */
@Composable
fun GameCard(
    game: UiLotofacilGame,
    index: Int,
    modifier: Modifier = Modifier,
    onAction: (String) -> Unit = {},
    onNumberItemRecompose: ((Int, Int) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Game $index",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NumberGrid(
                selectedNumbers = game.numbers,
                onNumberClick = { number ->
                    onAction("select_number_$number")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onItemRecompose = onNumberItemRecompose
            )
        }
    }
}
