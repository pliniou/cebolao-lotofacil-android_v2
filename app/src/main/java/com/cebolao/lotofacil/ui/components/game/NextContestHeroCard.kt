package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun NextContestHeroCard(
    info: NextDrawInfo?,
    modifier: Modifier = Modifier
) {
    if (info == null) return

    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true,
        // Using surfaceContainer with a tint from primary for a "hero" feel without being overpowering
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            // Header with Contest Number
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.padding(bottom = Dimen.Spacing8)
            ) {
                 Text(
                    text = stringResource(R.string.home_next_contest, info.contestNumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Dimen.Spacing16, vertical = Dimen.Spacing4)
                )
            }

            // Main Prize
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.widget_label_prize_estimate).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = Dimen.TrackingWidest
                )
                Text(
                    text = info.formattedPrize,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Date and Accumulation section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Box
                AppCard(
                    modifier = Modifier.weight(1f),
                    outlined = false,
                    color = MaterialTheme.colorScheme.surface,
                    contentPadding = Dimen.Spacing8
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.widget_label_draw_date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = info.formattedDate,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Final Five Accumulation Box
                AppCard(
                    modifier = Modifier.weight(1f),
                    outlined = false,
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    contentPadding = Dimen.Spacing8
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.next_contest_final_five_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = info.formattedPrizeFinalFive,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}