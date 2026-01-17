package com.cebolao.lotofacil.ui.components.stats

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sqrt

private const val Y_AXIS_WIDTH_PX = 60f
private const val X_AXIS_HEIGHT_PX = 60f
private const val TOP_PADDING_PX = 30f
private const val GRID_LINES = 4

enum class ChartType { BAR, LINE }

/**
 * Componente de gráfico customizado (Barra ou Linha) usando Canvas.
 *
 * Suporta animação de entrada, seleção por toque, destaque de valores e linha de distribuição normal.
 */
@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    maxValue: Int,
    modifier: Modifier = Modifier,
    chartHeight: Dp = Dimen.BarChartHeight,
    chartType: ChartType = ChartType.LINE,
    showNormalLine: Boolean = false,
    mean: Float? = null,
    stdDev: Float? = null,
    highlightValue: String? = null,
    highlightPredicate: ((Int) -> Boolean)? = null
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val animProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val colors = ChartColors(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.tertiary,
        highlight = MaterialTheme.colorScheme.error,
        text = MaterialTheme.colorScheme.onSurfaceVariant,
        line = MaterialTheme.colorScheme.outlineVariant,
        tooltipBg = MaterialTheme.colorScheme.surfaceContainerHighest,
        tooltipText = MaterialTheme.colorScheme.onSurface,
        normalLine = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )

    // Fallback seguro se a fonte não carregar no preview
    val typeface = remember {
        try {
            ResourcesCompat.getFont(context, R.font.stacksansnotch_bold) ?: Typeface.DEFAULT_BOLD
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }
    }
    val paints = remember(density, colors) { ChartPaints(density, colors, typeface) }

    LaunchedEffect(data) {
        selectedIndex = null
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(1000))
    }

    Box(
        modifier = modifier
            .height(chartHeight)
            .padding(vertical = Dimen.Spacing12)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        selectedIndex = ChartMetrics(size.toSize(), data.size).getBarIndexAt(offset.x)
                    }
                }
        ) {
            val metrics = ChartMetrics(size, data.size)
            drawGrid(maxValue, metrics, paints.axis, paints.grid)

            if (chartType == ChartType.BAR) {
                drawBars(
                    data = data,
                    max = max(1, maxValue),
                    m = metrics,
                    prog = animProgress.value,
                    selIdx = selectedIndex,
                    highlightVal = highlightValue,
                    highlightPred = highlightPredicate,
                    c = colors,
                    p = paints
                )
            } else {
                drawLineChart(
                    data = data,
                    max = max(1, maxValue),
                    m = metrics,
                    prog = animProgress.value,
                    selIdx = selectedIndex,
                    highlightVal = highlightValue,
                    highlightPred = highlightPredicate,
                    p = paints
                )
            }

            if (showNormalLine && mean != null && stdDev != null && stdDev > 0f) {
                drawNormalLine(
                    data = data,
                    mean = mean,
                    stdDev = stdDev,
                    m = metrics,
                    max = max(1, maxValue),
                    c = colors,
                    p = paints
                )
            }

            selectedIndex?.let { idx ->
                if (idx in data.indices) {
                    drawTooltip(
                        v = data[idx].second,
                        i = idx,
                        m = metrics,
                        max = max(1, maxValue),
                        prog = animProgress.value,
                        c = colors,
                        p = paints
                    )
                }
            }
        }
    }
}

@Immutable
private data class ChartColors(
    val primary: Color,
    val secondary: Color,
    val highlight: Color,
    val text: Color,
    val line: Color,
    val tooltipBg: Color,
    val tooltipText: Color,
    val normalLine: Color
)

@Stable
private class ChartMetrics(size: Size, val dataCount: Int) {
    val drawHeight = size.height - X_AXIS_HEIGHT_PX - TOP_PADDING_PX
    val yAxisX = Y_AXIS_WIDTH_PX
    val totalWidth = size.width - yAxisX - 40f

    val pointSpacing = if (dataCount > 1) totalWidth / (dataCount - 1) else totalWidth
    val touchTargetWidth = pointSpacing.coerceAtMost(60f)

    fun getX(index: Int): Float = yAxisX + (index * pointSpacing)

    fun getHeight(value: Int, max: Int): Float {
        if (max <= 0) return 0f
        return (value.toFloat() / max.toFloat()) * drawHeight
    }

    fun getBarIndexAt(x: Float): Int? {
        if (x < yAxisX - touchTargetWidth / 2) return null
        val relativeX = x - yAxisX
        val index = (relativeX / pointSpacing).roundToInt()
        return if (index in 0 until dataCount) index else null
    }

    private fun Float.roundToInt(): Int = round(this).toInt()
}

@Stable
private class ChartPaints(
    density: Density,
    colors: ChartColors,
    typeface: Typeface
) {
    val axis = Paint().apply {
        isAntiAlias = true
        color = colors.text.toArgb()
        textSize = density.run { 12.sp.toPx() }
        this.typeface = typeface
    }

    val grid = Paint().apply {
        isAntiAlias = true
        color = colors.line.toArgb()
        strokeWidth = density.run { 1.dp.toPx() }
        style = Paint.Style.STROKE
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    val label = Paint().apply {
        isAntiAlias = true
        color = colors.text.toArgb()
        textSize = density.run { 11.sp.toPx() }
        textAlign = Paint.Align.CENTER
        this.typeface = typeface
    }

    val linePaint = Paint().apply {
        isAntiAlias = true
        color = colors.primary.toArgb()
        strokeWidth = density.run { 3.dp.toPx() }
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    val dotPaint = Paint().apply {
        isAntiAlias = true
        color = colors.primary.toArgb()
        style = Paint.Style.FILL
    }

    val highlightDotPaint = Paint().apply {
        isAntiAlias = true
        color = colors.highlight.toArgb()
        style = Paint.Style.FILL
    }

    val tooltip = Paint().apply {
        isAntiAlias = true
        color = colors.tooltipText.toArgb()
        textSize = density.run { 14.sp.toPx() }
        textAlign = Paint.Align.CENTER
        this.typeface = typeface
    }
}

private fun DrawScope.drawGrid(
    max: Int,
    m: ChartMetrics,
    textPaint: Paint,
    gridPaint: Paint
) {
    val cappedMax = max.coerceAtLeast(1)
    val step = cappedMax / GRID_LINES.toFloat()
    for (i in 0..GRID_LINES) {
        val value = i * step
        val y = TOP_PADDING_PX + m.drawHeight - m.getHeight(value.toInt(), cappedMax)

        drawContext.canvas.nativeCanvas.drawLine(m.yAxisX, y, size.width, y, gridPaint)
        drawContext.canvas.nativeCanvas.drawText(
            value.toInt().toString(),
            m.yAxisX - 16f,
            y + 10f,
            textPaint.apply { textAlign = Paint.Align.RIGHT }
        )
    }
}

private fun DrawScope.drawBars(
    data: List<Pair<String, Int>>,
    max: Int,
    m: ChartMetrics,
    prog: Float,
    selIdx: Int?,
    highlightVal: String?,
    highlightPred: ((Int) -> Boolean)?,
    c: ChartColors,
    p: ChartPaints
) {
    val barWidth = (m.totalWidth / data.size.toFloat()) * 0.7f
    val barSpacing = m.totalWidth / data.size.toFloat()

    data.forEachIndexed { index, (label, value) ->
        val height = m.getHeight(value, max) * prog
        val x = m.yAxisX + (index * barSpacing) + (barSpacing - barWidth) / 2
        val y = TOP_PADDING_PX + m.drawHeight - height

        val isHighlighted = label == highlightVal
        val meetsPredicate = highlightPred?.invoke(value) ?: false
        val isSelected = selIdx == index

        val barColor = when {
            isHighlighted || meetsPredicate -> c.highlight
            isSelected -> c.secondary
            else -> c.primary
        }

        drawRect(
            color = barColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = Size(barWidth, height)
        )

        val labelWidthPx = 80f
        val maxLabels = (m.totalWidth / labelWidthPx).toInt().coerceAtLeast(1)
        val skipInterval = (data.size / maxLabels).coerceAtLeast(1)

        if (data.size <= 15 || index % skipInterval == 0 || index == data.size - 1) {
            drawContext.canvas.nativeCanvas.withSave {
                val centerX = x + barWidth / 2
                val baseY = size.height - 20f
                if (data.size > 15) {
                    rotate(-45f, centerX, baseY)
                    drawText(label, centerX, baseY, p.label.apply { textAlign = Paint.Align.RIGHT })
                } else {
                    drawText(label, centerX, baseY, p.label.apply { textAlign = Paint.Align.CENTER })
                }
            }
        }
    }
}

private fun DrawScope.drawLineChart(
    data: List<Pair<String, Int>>,
    max: Int,
    m: ChartMetrics,
    prog: Float,
    selIdx: Int?,
    highlightVal: String?,
    highlightPred: ((Int) -> Boolean)?,
    p: ChartPaints
) {
    val path = Path()
    data.forEachIndexed { index, (_, value) ->
        val height = m.getHeight(value, max) * prog
        val x = m.getX(index)
        val y = TOP_PADDING_PX + m.drawHeight - height
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), p.linePaint)

    data.forEachIndexed { index, (label, value) ->
        val height = m.getHeight(value, max) * prog
        val x = m.getX(index)
        val y = TOP_PADDING_PX + m.drawHeight - height

        val isHighlighted = label == highlightVal
        val meetsPredicate = highlightPred?.invoke(value) ?: false
        val isSelected = selIdx == index
        val showDot = isHighlighted || meetsPredicate || isSelected || (data.size <= 20) || (index % 3 == 0)

        if (showDot) {
            val dotRadius = if (isSelected || isHighlighted || meetsPredicate) 5.dp.toPx() else 3.dp.toPx()
            val paint = if (isHighlighted || meetsPredicate) p.highlightDotPaint else p.dotPaint
            drawContext.canvas.nativeCanvas.drawCircle(x, y, dotRadius, paint)
        }

        val labelWidthPx = 100f
        val maxLabels = (m.totalWidth / labelWidthPx).toInt().coerceAtLeast(1)
        val skipInterval = (data.size / maxLabels).coerceAtLeast(2)

        if (data.size <= 10 || index % skipInterval == 0 || index == data.size - 1) {
            drawContext.canvas.nativeCanvas.withSave {
                val baseY = size.height - 20f
                if (data.size > 10) {
                    rotate(-45f, x, baseY)
                    drawText(label, x, baseY, p.label.apply { textAlign = Paint.Align.RIGHT })
                } else {
                    drawText(label, x, baseY, p.label.apply { textAlign = Paint.Align.CENTER })
                }
            }
        }
    }
}

private fun DrawScope.drawTooltip(
    v: Int,
    i: Int,
    m: ChartMetrics,
    max: Int,
    prog: Float,
    c: ChartColors,
    p: ChartPaints
) {
    val xCenter = m.getX(i)
    val barTop = TOP_PADDING_PX + m.drawHeight - (m.getHeight(v, max) * prog) - 12f
    val text = v.toString()
    val width = p.tooltip.measureText(text) + 48f
    val rect = RoundRect(
        left = xCenter - width / 2f,
        top = barTop - 50f,
        right = xCenter + width / 2f,
        bottom = barTop,
        cornerRadius = CornerRadius(12f)
    )

    val path = Path().apply {
        addRoundRect(rect)
        moveTo(xCenter - 6f, barTop)
        lineTo(xCenter, barTop + 8f)
        lineTo(xCenter + 6f, barTop)
        close()
    }

    drawPath(path = path, color = c.tooltipBg)
    drawContext.canvas.nativeCanvas.drawText(text, xCenter, barTop - 18f, p.tooltip)
}

private fun DrawScope.drawNormalLine(
    data: List<Pair<String, Int>>,
    mean: Float,
    stdDev: Float,
    m: ChartMetrics,
    max: Int,
    c: ChartColors,
    p: ChartPaints
) {
    if (data.isEmpty()) return

    val totalCount = data.sumOf { it.second }
    val numericData = data.mapNotNull { (label, _) ->
        label.toFloatOrNull()
    }
    
    if (numericData.isEmpty()) return
    
    val dataRange = numericData.maxOrNull() ?: 0f - (numericData.minOrNull() ?: 0f)
    val bucketWidth = if (dataRange > 0) dataRange / (numericData.size - 1).coerceAtLeast(1) else 1f

    val path = Path()
    var started = false

    // Calcular distribuição normal para cada ponto de dados
    data.forEachIndexed { i, (label, _) ->
        val xCenter = m.getX(i)
        val value = label.toFloatOrNull() ?: return@forEachIndexed

        val z = (value - mean) / stdDev
        val pdf = (1f / (stdDev * sqrt(2f * PI.toFloat()))) * exp(-0.5f * z * z)
        
        // Ajustar a escala para melhor visualização
        val scaleFactor = when {
            bucketWidth > 10f -> 0.8f  // Para ranges grandes (como soma)
            bucketWidth > 2f -> 1.2f   // Para ranges médios
            else -> 1.5f              // Para ranges pequenos (como contagens)
        }
        
        val predictedCount = (totalCount * bucketWidth * pdf * scaleFactor).toInt().coerceAtLeast(0)
        val y = TOP_PADDING_PX + m.drawHeight - m.getHeight(predictedCount, max)

        if (!started) {
            path.moveTo(xCenter, y); started = true
        } else {
            path.lineTo(xCenter, y)
        }
    }

    drawPath(
        path = path,
        color = c.normalLine,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 5.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 8f), 0f)
        )
    )

    drawContext.canvas.nativeCanvas.drawText(
        "Distribuição Normal",
        size.width - 200f,
        TOP_PADDING_PX + 20f,
        p.label.apply {
            textAlign = Paint.Align.LEFT
            color = c.normalLine.toArgb()
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun BarChartPreview() {
    val mockData = (1..10).map { it.toString() to (it * 10) }.toImmutableList()
    MaterialTheme {
        Box(modifier = Modifier.padding(Dimen.Spacing16).background(MaterialTheme.colorScheme.surface)) {
            BarChart(
                data = mockData,
                maxValue = 100,
                modifier = Modifier.fillMaxSize().height(200.dp),
                chartType = ChartType.BAR
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LineChartPreview() {
    val mockData = (1..10).map { it.toString() to (it * 5 + 20) }.toImmutableList()
    MaterialTheme {
        Box(modifier = Modifier.padding(Dimen.Spacing16).background(MaterialTheme.colorScheme.surface)) {
            BarChart(
                data = mockData,
                maxValue = 100,
                modifier = Modifier.fillMaxSize().height(200.dp),
                chartType = ChartType.LINE,
                showNormalLine = true,
                mean = 5.5f,
                stdDev = 2.0f
            )
        }
    }
}