package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun NextContestHeroCard(
    info: NextDrawInfo?,
    modifier: Modifier = Modifier
) {
    if (info == null) return

    val scheme = MaterialTheme.colorScheme
    
    // Gradient or solid primary for that "official" look
    val containerColor = scheme.primary
    val userContentColor = scheme.onPrimary

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = userContentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.Elevation.None)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.Spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            // Header: NEXT CONTEST 1234
            Text(
                text = stringResource(R.string.home_next_contest, info.contestNumber).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = userContentColor.copy(alpha = 0.8f),
                letterSpacing = Dimen.TrackingWidest
            )

            // Prize Value (Hero)
            Text(
                text = info.formattedPrize,
                style = MaterialTheme.typography.displaySmall, // Bigger
                fontWeight = FontWeight.ExtraBold,
                color = userContentColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.widget_label_prize_estimate),
                style = MaterialTheme.typography.labelSmall,
                color = userContentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(Dimen.Spacing8))

            // Footer: Date | Final 5
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = userContentColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(Dimen.Spacing8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Date
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.widget_label_draw_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = userContentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = info.formattedDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = userContentColor
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .size(Dimen.Border.Thin, Dimen.IconMedium)
                        .background(userContentColor.copy(alpha = 0.2f))
                )

                // Final 5
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.next_contest_final_five_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = userContentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = info.formattedPrizeFinalFive,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = userContentColor
                    )
                }
            }
        }
    }
}
