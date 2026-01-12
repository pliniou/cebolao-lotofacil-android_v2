package com.cebolao.lotofacil.ui.components.telemetry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.domain.service.GenerationTelemetry
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Dashboard compacto de telemetria
 * Exibe métricas de performance da última geração
 */
@Composable
fun TelemetryDashboard(
    telemetry: GenerationTelemetry?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = telemetry != null,
        enter = fadeIn(animationSpec = Motion.Tween.enter()) +
                expandVertically(animationSpec = Motion.Spring.gentle()),
        exit = fadeOut(animationSpec = Motion.Tween.exit()) +
               shrinkVertically(animationSpec = Motion.Spring.gentle()),
        modifier = modifier
    ) {
        telemetry?.let {
            AppCard(
                variant = CardVariant.Solid,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(Dimen.IconSmall),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Métricas de Performance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                    ) {
                        // Success Rate
                        MetricItem(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            label = "Taxa de Sucesso",
                            value = "${"%.1f".format(it.successRate * 100)}%",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Avg Time
                        MetricItem(
                            icon = Icons.Default.Speed,
                            label = "Tempo Médio",
                            value = "${it.avgTimePerGame}ms",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Total Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
                    ) {
                        StatText(
                            label = "Tentativas",
                            value = "${it.totalAttempts}"
                        )
                        StatText(
                            label = "Duração",
                            value = "${it.durationMs}ms"
                        )
                        if (it.mostRestrictiveFilter != null) {
                            StatText(
                                label = "Filtro Restritivo",
                                value = it.mostRestrictiveFilter.toString()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(Dimen.Spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimen.IconSmall),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatText(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}