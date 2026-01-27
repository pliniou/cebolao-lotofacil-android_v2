package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.domain.model.GameScore
import com.cebolao.lotofacil.domain.model.MetricEvaluation
import com.cebolao.lotofacil.domain.model.ScoreStatus
import com.cebolao.lotofacil.ui.theme.GlassCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.SuccessColor
import com.cebolao.lotofacil.ui.theme.SuccessBase
import com.cebolao.lotofacil.ui.theme.WarningBase
import com.cebolao.lotofacil.ui.theme.ErrorColor

@Composable
fun GameQualityCard(
    score: GameScore,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
        ) {
            // Header: Total Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(com.cebolao.lotofacil.R.string.game_quality_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(getStatusDescriptionRes(score.status)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ScoreBadge(score.totalScore, score.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Metrics List
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
            ) {
                score.evaluations.forEach { evaluation ->
                    MetricRow(evaluation)
                }
            }
        }
    }
}

@Composable
private fun ScoreBadge(score: Int, status: ScoreStatus) {
    val color = getStatusColor(status)

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(Dimen.CornerRadiusMedium))
            .padding(horizontal = Dimen.Spacing12, vertical = Dimen.Spacing8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score%",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
private fun MetricRow(metric: MetricEvaluation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot indicator
        Box(
            modifier = Modifier
                .size(Dimen.IndicatorHeightSmall)
                .clip(CircleShape)
                .background(getStatusColor(metric.status))
        )

        Spacer(modifier = Modifier.width(Dimen.Spacing8))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = metric.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = metric.value.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = metric.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getStatusColor(status: ScoreStatus): Color {
    return when (status) {
        ScoreStatus.EXCELLENT -> SuccessColor
        ScoreStatus.GOOD -> SuccessBase
        ScoreStatus.WARNING -> WarningBase
        ScoreStatus.BAD -> ErrorColor
    }
}

private fun getStatusDescriptionRes(status: ScoreStatus): Int {
    return when (status) {
        ScoreStatus.EXCELLENT -> com.cebolao.lotofacil.R.string.game_quality_description_excellent
        ScoreStatus.GOOD -> com.cebolao.lotofacil.R.string.game_quality_description_good
        ScoreStatus.WARNING -> com.cebolao.lotofacil.R.string.game_quality_description_warning
        ScoreStatus.BAD -> com.cebolao.lotofacil.R.string.game_quality_description_bad
    }
}

@Preview(showBackground = true)
@Composable
internal fun GameQualityCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            GameQualityCard(
                score = GameScore(
                    totalScore = 85,
                    status = ScoreStatus.GOOD,
                    evaluations = listOf(
                        MetricEvaluation(
                            name = "Pares",
                            value = 7,
                            status = ScoreStatus.EXCELLENT,
                            message = "Dentro do padrão (7-8)"
                        ),
                        MetricEvaluation(
                            name = "Primos",
                            value = 4,
                            status = ScoreStatus.WARNING,
                            message = "Um pouco abaixo (5-6)"
                        )
                    )
                )
            )
        }
    }
}
