package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EnhancedStatsCard(
    gameMetrics: com.cebolao.lotofacil.domain.model.GameComputedMetrics,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        title = "Análise Estatística",
        outlined = true
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
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
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
    ) {
        EnhancedMetricItem(
            label = "Soma",
            value = metrics.sum.toString(),
            maxValue = 300, // Soma máxima possível
            currentValue = metrics.sum.toFloat(),
            color = scheme.primary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = "Pares",
            value = metrics.evens.toString(),
            maxValue = 15,
            currentValue = metrics.evens.toFloat(),
            color = scheme.secondary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = "Primos",
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
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
    ) {
        EnhancedMetricItem(
            label = "Fibonacci",
            value = metrics.fibonacci.toString(),
            maxValue = 15,
            currentValue = metrics.fibonacci.toFloat(),
            color = scheme.primary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = "Moldura",
            value = metrics.frame.toString(),
            maxValue = 15,
            currentValue = metrics.frame.toFloat(),
            color = scheme.secondary,
            modifier = Modifier.weight(1f)
        )
        EnhancedMetricItem(
            label = "Centro",
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
                .size(40.dp, 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(scheme.surfaceVariant)
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
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
    val chartData = remember(metrics) {
        listOf(
            ChartData("Soma", metrics.sum, 300, scheme.primary),
            ChartData("Pares", metrics.evens, 15, scheme.secondary),
            ChartData("Primos", metrics.primes, 15, scheme.tertiary),
            ChartData("Fibonacci", metrics.fibonacci, 15, scheme.primary),
            ChartData("Moldura", metrics.frame, 15, scheme.secondary),
            ChartData("Centro", metrics.center, 15, scheme.tertiary)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = Dimen.Spacing8)
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
    val color: Color
)

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
            color = android.graphics.Color.WHITE
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
            color = android.graphics.Color.DKGRAY
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
