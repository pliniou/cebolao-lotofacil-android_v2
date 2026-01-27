package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.model.UiNextDrawInfo
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.GradientAzul

@Composable
fun NextContestHeroCard(
    info: UiNextDrawInfo?,
    modifier: Modifier = Modifier
) {
    if (info == null) return

    val userContentColor = Color.White
    
    // Premium Hero Card with Gradient
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(brush = GradientAzul)
            .border(
                width = Dimen.Border.Thin,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = MaterialTheme.shapes.medium
            )
            .padding(Dimen.Spacing16)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Header: NEXT CONTEST 1234
            Text(
                text = stringResource(
                    R.string.home_next_contest,
                    info.contestNumber,
                    info.formattedDate
                ).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = userContentColor.copy(alpha = 0.9f),
                letterSpacing = Dimen.TrackingWidest
            )

            // Prize Value (Hero)
            Text(
                text = info.formattedPrize,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = userContentColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.widget_label_prize_estimate),
                style = MaterialTheme.typography.labelSmall,
                color = userContentColor.copy(alpha = 0.8f)
            )

            // Footer: Date | Final 5
            Row(
                modifier = Modifier
                    .padding(top = Dimen.Spacing8)
                    .background(
                        color = Color.Black.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(
                        width = Dimen.Border.Hairline,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = Dimen.Spacing12, vertical = Dimen.Spacing8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
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
                        .size(1.dp, Dimen.IconMedium)
                        .background(userContentColor.copy(alpha = 0.3f))
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
