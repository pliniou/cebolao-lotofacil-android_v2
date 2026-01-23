package com.cebolao.lotofacil.ui.components.game

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.model.UiLotofacilGame
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen

@Stable
@Composable
fun GameCard(
    game: UiLotofacilGame,
    index: Int,
    modifier: Modifier = Modifier,
    onAction: (GameCardAction) -> Unit,
    onNumberItemRecompose: ((number: Int, count: Int) -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val isPinned = game.isPinned

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = if (isPinned) CardVariant.Outlined else CardVariant.Solid,
        color = if (isPinned) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
        } else {
            Color.Unspecified
        },
        contentPadding = Dimen.Spacing12
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.game_card_title_format, index),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )

                IconButton(onClick = { onAction(GameCardAction.Pin) }, modifier = Modifier.testTag("game_card_pin_$index")) {
                    Icon(
                        imageVector = if (isPinned) AppIcons.PinFilled else AppIcons.PinOutlined,
                        contentDescription = stringResource(
                            if (isPinned) R.string.game_card_action_unpin else R.string.game_card_action_pin
                        ),
                        tint = if (isPinned) scheme.primary else scheme.onSurfaceVariant
                    )
                }
            }

            NumberGrid(
                selectedNumbers = game.numbers,
                onNumberClick = {},
                modifier = Modifier.fillMaxWidth(),
                maxSelection = GameConstants.GAME_SIZE,
                sizeVariant = NumberBallSize.Small,
                ballVariant = if (isPinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral,
                onItemRecompose = onNumberItemRecompose
            )

            GameCardActions(
                onDelete = { onAction(GameCardAction.Delete) },
                onShare = { onAction(GameCardAction.Share) },
                onCheck = { onAction(GameCardAction.Check) }
            )
        }
    }
}

@Composable
private fun GameCardActions(
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCheck: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing4, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShare) {
            Icon(
                imageVector = AppIcons.Share,
                contentDescription = stringResource(R.string.games_share_chooser_title),
                tint = scheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = AppIcons.Delete,
                contentDescription = stringResource(R.string.games_delete_confirm),
                tint = scheme.error
            )
        }
        IconButton(onClick = onCheck) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = stringResource(R.string.game_card_action_check),
                tint = scheme.primary
            )
        }
    }
}
