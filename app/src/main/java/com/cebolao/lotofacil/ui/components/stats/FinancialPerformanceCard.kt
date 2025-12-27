package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.domain.model.FinancialProjection
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters
import java.math.BigDecimal
import java.util.Locale

@Composable
fun FinancialPerformanceCard(
    projection: FinancialProjection,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    
    val isProfit = projection.profitDecimal >= BigDecimal.ZERO
    val profitColor = if (isProfit) Color(0xFF4CAF50) else scheme.error
    
    // Calculate progress for visual bar (capped at 1.0)
    // If investment is 0, avoid division by zero
    val progress = remember(projection.investmentDecimal, projection.revenueDecimal) {
        if (projection.investmentDecimal.compareTo(BigDecimal.ZERO) == 0) {
            if (projection.revenueDecimal > BigDecimal.ZERO) 1f else 0f
        } else {
            val ratio = projection.revenueDecimal.toFloat() / projection.investmentDecimal.toFloat()
            ratio.coerceIn(0f, 1f)
        }
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
         Column(
             modifier = Modifier.padding(Dimen.Spacing16),
             verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
         ) {
             // Header
             Row(
                 verticalAlignment = Alignment.CenterVertically, 
                 horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
             ) {
                 Box(
                     modifier = Modifier
                         .size(32.dp)
                         .background(scheme.primaryContainer, CircleShape),
                     contentAlignment = Alignment.Center
                 ) {
                      Icon(
                         imageVector = Icons.Outlined.AttachMoney, 
                         contentDescription = null, 
                         tint = scheme.onPrimaryContainer,
                         modifier = Modifier.size(20.dp)
                     )
                 }
                 Column {
                     Text(
                         text = "Simulação Financeira",
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = scheme.onSurface
                     )
                     Text(
                         text = "Custo atual de R$ 3,50 por aposta", // Could be dynamic
                         style = MaterialTheme.typography.bodySmall,
                         color = scheme.onSurfaceVariant
                     )
                 }
             }
             
             AppDivider()

             // Stats Grid
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.SpaceBetween
             ) {
                FinancialMetric(
                    label = "Investimento Total",
                    value = Formatters.formatCurrency(projection.investmentDecimal),
                )
                FinancialMetric(
                    label = "Prêmios (Retorno)",
                    value = Formatters.formatCurrency(projection.revenueDecimal),
                    color = if (projection.revenueDecimal > BigDecimal.ZERO) scheme.primary else null
                )
             }
             
             // Visual Bar
             Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween
                 ) {
                     Text(
                         text = "Recuperação do Investimento",
                         style = MaterialTheme.typography.labelSmall,
                         color = scheme.onSurfaceVariant
                     )
                     Text(
                         text = "${(progress * 100).toInt()}%",
                         style = MaterialTheme.typography.labelSmall,
                         fontWeight = FontWeight.Bold,
                         color = scheme.onSurface
                     )
                 }
                 LinearProgressIndicator(
                     progress = { progress },
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(8.dp)
                         .clip(MaterialTheme.shapes.small),
                     color = if (isProfit) Color(0xFF4CAF50) else scheme.primary,
                     trackColor = scheme.surfaceContainerHighest,
                     strokeCap = StrokeCap.Round,
                 )
             }
             
             // Outcome Box (Profit/Loss)
             Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(profitColor.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                     .padding(Dimen.Spacing12)
             ) {
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Row(
                         horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Icon(
                             imageVector = if (isProfit) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                             contentDescription = null,
                             tint = profitColor,
                             modifier = Modifier.size(24.dp)
                         )
                         Column {
                             Text(
                                text = if (isProfit) "LUCRO LÍQUIDO" else "PREJUÍZO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = profitColor
                             )
                             Text(
                                text = Formatters.formatCurrency(projection.profitDecimal.abs()), // Show absolute value
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = profitColor
                             )
                         }
                     }
                     
                     // ROI Chip
                     SurfaceTag(
                         text = "ROI ${String.format(Locale.US, "%.1f", projection.roi)}%",
                         color = profitColor
                     )
                 }
             }
         }
    }
}

@Composable
private fun FinancialMetric(
    label: String,
    value: String,
    color: Color? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SurfaceTag(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
