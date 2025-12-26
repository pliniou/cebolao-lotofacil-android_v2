package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    
    val profitColor = if (projection.profitDecimal >= BigDecimal.ZERO) 
        Color(0xFF4CAF50) 
    else 
        scheme.error

    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
         Column(verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)) {
             Row(
                 verticalAlignment = Alignment.CenterVertically, 
                 horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
             ) {
                 Icon(
                     imageVector = Icons.Outlined.AttachMoney, 
                     contentDescription = null, 
                     tint = scheme.primary
                 )
                 Text(
                     text = "Simulação Financeira",
                     style = MaterialTheme.typography.titleSmall,
                     fontWeight = FontWeight.SemiBold,
                     color = scheme.onSurface
                 )
             }
             
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.SpaceBetween
             ) {
                FinancialMetric(
                    label = "Investimento",
                    value = Formatters.formatCurrency(projection.investmentDecimal),
                )
                FinancialMetric(
                    label = "Retorno Estimado",
                    value = Formatters.formatCurrency(projection.revenueDecimal),
                    color = null
                )
             }
             
             AppDivider()
             
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.SpaceBetween,
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Column {
                     Text(
                        text = "Saldo Final",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                     )
                     Text(
                        text = Formatters.formatCurrency(projection.profitDecimal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = profitColor
                     )
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

@Composable
private fun FinancialMetric(
    label: String,
    value: String,
    color: Color? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
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
            .background(color.copy(alpha = 0.15f), MaterialTheme.shapes.extraSmall)
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
