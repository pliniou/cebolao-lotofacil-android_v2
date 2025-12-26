package com.cebolao.lotofacil.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.domain.model.GameScore
import com.cebolao.lotofacil.domain.model.MetricEvaluation
import com.cebolao.lotofacil.domain.model.ScoreStatus
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun GameQualityCard(
    score: GameScore,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = false,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            // Header: Total Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Qualidade do Jogo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = getStatusDescription(score.status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ScoreBadge(score.totalScore, score.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Metrics List
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
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
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
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
                .size(8.dp)
                .clip(CircleShape)
                .background(getStatusColor(metric.status))
        )
        
        Spacer(modifier = Modifier.width(Dimen.Spacing12))
        
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
        ScoreStatus.EXCELLENT -> Color(0xFF4CAF50) // Green
        ScoreStatus.GOOD -> Color(0xFF8BC34A) // Light Green
        ScoreStatus.WARNING -> Color(0xFFFFC107) // Amber
        ScoreStatus.BAD -> Color(0xFFF44336) // Red
    }
}

private fun getStatusDescription(status: ScoreStatus): String {
    return when (status) {
        ScoreStatus.EXCELLENT -> "Excelente probabilidade estatística"
        ScoreStatus.GOOD -> "Boas chances estatísticas"
        ScoreStatus.WARNING -> "Alguns desvios do padrão"
        ScoreStatus.BAD -> "Estatisticamente improvável"
    }
}
