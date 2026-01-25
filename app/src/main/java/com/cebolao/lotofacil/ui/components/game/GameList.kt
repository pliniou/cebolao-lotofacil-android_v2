package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.common.MessageState
import com.cebolao.lotofacil.ui.components.layout.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.model.UiLotofacilGame
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun GameList(
    games: List<UiLotofacilGame>,
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit,
    onAction: (GameCardAction, UiLotofacilGame) -> Unit
) {
    val gridState = rememberLazyGridState()
    val animateCards by remember(games.size) { derivedStateOf { games.size < 40 } }

    if (games.isEmpty()) {
        EmptyState(isNewGamesTab, onGenerateRequest)
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .widthIn(max = Dimen.LayoutMaxWidth)
                .fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(
                top = Dimen.ItemSpacing,
                bottom = Dimen.SectionSpacing,
                start = Dimen.ScreenPadding,
                end = Dimen.ScreenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing),
            horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            itemsIndexed(
                items = games,
                key = { _, game -> game.mask },
                contentType = { _, _ -> "game_card" }
            ) { index, game ->
                val card: @Composable () -> Unit = {
                    GameCard(
                        game = game,
                        index = index + 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Dimen.GameCardMinHeight),
                        onAction = { action -> onAction(action, game) }
                    )
                }
                if (animateCards) {
                    AnimateOnEntry(delayMillis = (index * 40).toLong()) { card() }
                } else {
                    card()
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimen.ScreenPadding,
                vertical = Dimen.SectionSpacing
            ),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            outlined = false,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            MessageState(
                icon = AppIcons.List,
                title = stringResource(R.string.games_empty_state_title),
                message = stringResource(
                    if (isNewGamesTab) R.string.games_empty_state_description
                    else R.string.widget_no_pinned_games
                ),
                actionLabel = if (isNewGamesTab) stringResource(R.string.filters_button_generate) else null,
                onActionClick = if (isNewGamesTab) onGenerateRequest else null,
                modifier = Modifier
            )
        }
    }
}
