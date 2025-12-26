package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.cebolao.lotofacil.ui.components.layout.AppCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.NextDrawInfo
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f), // Subtle background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            // N�vel 1: N�mero do concurso (topo)
            Text(
                text = stringResource(
                    R.string.home_next_contest,
                    info.contestNumber
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.ExtraBold
            )

            // N�vel 2: Valor do pr�mio (centro - destaque principal)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.SpacingTiny)
            ) {
                Text(
                    text = info.formattedPrize,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // N�vel 3: Data e Acumulado (base)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Data do sorteio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.MEDIUM)
                    )
                    Text(
                        text = info.formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(
                            horizontal = Dimen.Spacing16,
                            vertical = Dimen.Spacing4
                        )
                    ) {
                        Text(
                            text = "Acumulado final 0/5",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = Alpha.MEDIUM)
                        )
                        Text(
                            text = info.formattedPrizeFinalFive,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}