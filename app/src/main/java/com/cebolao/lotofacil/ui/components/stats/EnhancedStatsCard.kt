package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun EnhancedStatsCard(
    gameMetrics: com.cebolao.lotofacil.domain.model.GameComputedMetrics,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        title = stringResource(R.string.enhanced_stats_title),
        outlined = true
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
        ) {
            // Métricas principais com visualização aprimorada
            EnhancedMetricsRow(gameMetrics)
            
            // Gráfico de barras para comparação visual
            MetricsBarChart(gameMetrics)
        }
    }
}

@Composable
private fun EnhancedMetricsRow(
    metrics: com.cebolao.lotofacil.domain.model.GameComputedMetrics
) {
    val scheme = MaterialTheme.colorScheme
    
    // Primeira linha com as métricas mais importantes
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        EnhancedMetricItem(
            label = stringResource(R.string.stat_sum),
            value = metrics.sum.toString(),
            maxValue = 300, // Soma máxima possível
            currentValue = metrics.sum.toFloat(),
            color = scheme.primary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = stringResource(R.string.stat_evens),
            value = metrics.evens.toString(),
            maxValue = 15,
            currentValue = metrics.evens.toFloat(),
            color = scheme.secondary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = stringResource(R.string.stat_primes),
            value = metrics.primes.toString(),
            maxValue = 15,
            currentValue = metrics.primes.toFloat(),
            color = scheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
    
    // Segunda linha com métricas secundárias
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        EnhancedMetricItem(
            label = stringResource(R.string.stat_fibonacci),
            value = metrics.fibonacci.toString(),
            maxValue = 15,
            currentValue = metrics.fibonacci.toFloat(),
            color = scheme.primary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = stringResource(R.string.stat_frame),
            value = metrics.frame.toString(),
            maxValue = 15,
            currentValue = metrics.frame.toFloat(),
            color = scheme.secondary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = stringResource(R.string.stat_center),
            value = metrics.center.toString(),
            maxValue = 15,
            currentValue = metrics.center.toFloat(),
            color = scheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EnhancedMetricItem(
    label: String,
    value: String,
    maxValue: Int,
    currentValue: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val progress = remember(currentValue, maxValue) { 
        (currentValue / maxValue.toFloat()).coerceIn(0f, 1f)
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Valor principal
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant
        )
        
        // Barra de progresso circular
        Box(
            modifier = Modifier
                .padding(top = Dimen.Spacing4)
                .size(Dimen.MiniBarWidth, Dimen.MiniBarHeight)
                .clip(RoundedCornerShape(Dimen.CornerRadiusTiny))
                .background(scheme.surfaceVariant)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.MiniBarHeight),
                color = color,
                trackColor = scheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun MetricsBarChart(
    metrics: com.cebolao.lotofacil.domain.model.GameComputedMetrics
) {
    val scheme = MaterialTheme.colorScheme
    val labelColor = scheme.onSurfaceVariant
    val chartData = listOf(
        ChartData(stringResource(R.string.stat_sum), metrics.sum, 300, scheme.primary, labelColor),
        ChartData(stringResource(R.string.stat_evens), metrics.evens, 15, scheme.secondary, labelColor),
        ChartData(stringResource(R.string.stat_primes), metrics.primes, 15, scheme.tertiary, labelColor),
        ChartData(stringResource(R.string.stat_fibonacci), metrics.fibonacci, 15, scheme.primary, labelColor),
        ChartData(stringResource(R.string.stat_frame), metrics.frame, 15, scheme.secondary, labelColor),
        ChartData(stringResource(R.string.stat_center), metrics.center, 15, scheme.tertiary, labelColor)
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimen.ChartHeightMini)
            .padding(horizontal = Dimen.Spacing4)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEnhancedBarChart(chartData, size)
        }
    }
}

private data class ChartData(
    val label: String,
    val value: Int,
    val maxValue: Int,
    val color: Color,
    val labelColor: Color
) {
    val valueTextColor: Color
        get() = if (color.luminance() < 0.5f) Color.White else Color.Black
}

private fun DrawScope.drawEnhancedBarChart(
    data: List<ChartData>,
    canvasSize: Size
) {
    val barWidth = canvasSize.width / (data.size * 2f)
    val barSpacing = barWidth
    val maxHeight = canvasSize.height - 40f
    val baseY = canvasSize.height - 20f
    
    data.forEachIndexed { index, chartData ->
        val barHeight = (chartData.value.toFloat() / chartData.maxValue) * maxHeight
        val x = barSpacing + (index * barSpacing * 2f)
        val y = baseY - barHeight
        
        // Desenhar barra com gradiente
        drawRoundRect(
            color = chartData.color,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(4f)
        )
        
        // Desenhar valor no topo da barra
        val textPaint = android.graphics.Paint().apply {
            color = chartData.valueTextColor.toArgb()
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        drawContext.canvas.nativeCanvas.drawText(
            chartData.value.toString(),
            x + barWidth / 2f,
            y - 5f,
            textPaint
        )
        
        // Desenhar label abaixo
        val labelPaint = android.graphics.Paint().apply {
            color = chartData.labelColor.toArgb()
            textSize = 20f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        drawContext.canvas.nativeCanvas.drawText(
            chartData.label,
            x + barWidth / 2f,
            baseY + 15f,
            labelPaint
        )
    }
}
