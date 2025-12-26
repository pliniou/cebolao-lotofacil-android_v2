package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SimpleStatsCard(
    stats: ImmutableList<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true,
        contentPadding = Dimen.Spacing16
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            Text(
                text = stringResource(R.string.checker_simple_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER))

            stats.forEachIndexed { index, (label, value) ->
                InfoValueRow(label = label, value = value)
                if (index < stats.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER)
                    )
                }
            }
        }
    }
}
