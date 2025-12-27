package com.cebolao.lotofacil.ui.components.stats

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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

private const val Y_AXIS_WIDTH_PX = 70f
private const val X_AXIS_HEIGHT_PX = 70f
private const val TOP_PADDING_PX = 40f
private const val GRID_LINES = 4

@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    maxValue: Int,
    modifier: Modifier = Modifier,
    chartHeight: Dp = Dimen.BarChartHeight,
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
        highlight = MaterialTheme.colorScheme.error, // Destaque (ex: último concurso)
        text = MaterialTheme.colorScheme.onSurfaceVariant,
        line = MaterialTheme.colorScheme.outlineVariant,
        tooltipBg = MaterialTheme.colorScheme.surfaceContainerHighest,
        tooltipText = MaterialTheme.colorScheme.onSurface,
        normalLine = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )

    val typeface = remember {
        ResourcesCompat.getFont(context, R.font.stacksansnotch_bold) ?: Typeface.DEFAULT_BOLD
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
            .padding(vertical = Dimen.Spacing16)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        selectedIndex =
                            ChartMetrics(size.toSize(), data.size).getBarIndexAt(offset.x)
                    }
                }
        ) {
            val metrics = ChartMetrics(size, data.size)
            drawGrid(maxValue, metrics, paints.axis, paints.grid, colors.line)
            drawBars(
                data = data,
                max = maxValue,
                m = metrics,
                prog = animProgress.value,
                selIdx = selectedIndex,
                highlightVal = highlightValue,
                highlightPred = highlightPredicate,
                c = colors,
                p = paints
            )
            
            if (showNormalLine && mean != null && stdDev != null && stdDev > 0) {
                 drawNormalLine(
                     data = data,
                     mean = mean,
                     stdDev = stdDev,
                     m = metrics,
                     max = maxValue,
                     c = colors
                 )
            }

            selectedIndex?.let { idx ->
                if (idx in data.indices) {
                    drawTooltip(
                        v = data[idx].second,
                        i = idx,
                        m = metrics,
                        max = maxValue,
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
private class ChartMetrics(
    size: Size,
    val dataCount: Int
) {
// Replaced drawBars with drawLineChart logic inside the main component or a new function
// For clarity, I'm replacing the entire file content effectively with the new implementation in the next step
// But since this tool requires contiguous block, I will replace the internal logic first.

// NOTE: Strategy change. I will rewrite the whole file content to be clean, 
// but using replace_file_content on a large file is risky if I don't match exactly.
// I will target the ChartMetrics class first to adjust spacing.

    val drawHeight = size.height - X_AXIS_HEIGHT_PX - TOP_PADDING_PX
    val yAxisX = Y_AXIS_WIDTH_PX
    val totalWidth = size.width - yAxisX - 40f // Margin right
    
    // Line Chart specific metrics
    // We want points distributed evenly.
    // If we have N points, we have N-1 segments.
    // Spacing is totalWidth / (count - 1) if count > 1
    
    val pointSpacing = if (dataCount > 1) totalWidth / (dataCount - 1) else totalWidth
    
    // For hit testing (barWidth concept replaced by touch target width)
    val touchTargetWidth = pointSpacing.coerceAtMost(60f) 

    fun getX(index: Int): Float {
        return yAxisX + (index * pointSpacing)
    }

    fun getHeight(value: Int, max: Int): Float {
        if (max == 0) return 0f
        return (value.toFloat() / max.toFloat()) * drawHeight
    }
    
    fun getHeight(value: Double, max: Int) = getHeight(value.toInt(), max)
    fun getHeight(value: Float, max: Int) = getHeight(value.toInt(), max)

    fun getBarIndexAt(x: Float): Int? {
        if (x < yAxisX - touchTargetWidth/2) return null
        
        // Find nearest index
        val relativeX = x - yAxisX
        val index = (relativeX / pointSpacing).roundToInt()
        
        return if (index in 0 until dataCount) index else null
    }

    // Helper for rounding since we are in a class without imports potentially or use kotlin.math
    private fun Float.roundToInt(): Int = kotlin.math.round(this).toInt()
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
        setTypeface(typeface)
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
        setTypeface(typeface)
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
        setTypeface(typeface)
    }
}

private fun DrawScope.drawGrid(
    max: Int,
    m: ChartMetrics,
    textPaint: Paint, 
    gridPaint: Paint,
    lineColor: Color
) {
    val step = max / GRID_LINES.toFloat()
    
    for (i in 0..GRID_LINES) {
        val value = i * step
        val y = TOP_PADDING_PX + m.drawHeight - m.getHeight(value.toInt(), max)
        
        // Grid Line
        drawContext.canvas.nativeCanvas.drawLine(
            m.yAxisX, y, size.width, y, gridPaint
        )

        // Axis Label
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
    // Draw Line
    val path = Path()
    data.forEachIndexed { index, (_, value) ->
        val height = m.getHeight(value, max) * prog
        val x = m.getX(index)
        val y = TOP_PADDING_PX + m.drawHeight - height
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            // Bezier curve for smoothness? Or straight lines? 
            // Request says "improve chart... spacing... line chart".
            // Straight lines are clearer for discreet data usually, but catmull-rom is nice.
            // Let's stick to straight lines for accuracy of reading.
            path.lineTo(x, y)
        }
    }
    
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), p.linePaint)
    
    // Draw Dots
    data.forEachIndexed { index, (label, value) ->
        val height = m.getHeight(value, max) * prog
        val x = m.getX(index)
        val y = TOP_PADDING_PX + m.drawHeight - height
        
        val isHighlighted = label == highlightVal
        val meetsPredicate = highlightPred?.invoke(value) ?: false
        val isSelected = selIdx == index
        
        val dotRadius = if (isSelected || isHighlighted || meetsPredicate) 6.dp.toPx() else 4.dp.toPx()
        val paint = if (isHighlighted || meetsPredicate) p.highlightDotPaint else if (isSelected) p.highlightDotPaint else p.dotPaint
        
        drawContext.canvas.nativeCanvas.drawCircle(x, y, dotRadius, paint)
        
        // Label logic
        val labelWidthPx = 80f
        val maxLabels = (m.totalWidth / labelWidthPx).toInt().coerceAtLeast(1)
        val skipInterval = (data.size / maxLabels).coerceAtLeast(1)
        
        if (data.size <= 15 || index % skipInterval == 0 || index == data.size - 1) {
             drawContext.canvas.nativeCanvas.withSave {
                val baseY = size.height - 20f
                if (data.size > 15) {
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
    val barTop =
        TOP_PADDING_PX + m.drawHeight - (m.getHeight(v, max) * prog) - 12f
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
    drawContext.canvas.nativeCanvas.drawText(
        text,
        xCenter,
        barTop - 18f,
        p.tooltip
    )
}

private fun DrawScope.drawNormalLine(
    data: List<Pair<String, Int>>,
    mean: Float,
    stdDev: Float,
    m: ChartMetrics,
    max: Int,
    c: ChartColors
) {
    if (data.isEmpty()) return

    val totalCount = data.sumOf { it.second }
    // Estimate bucket width from first two items if available, else 10 (default for sum)
    val bucketWidth = if (data.size > 1) {
         val v0 = data[0].first.toFloatOrNull()
         val v1 = data[1].first.toFloatOrNull()
         if (v0 != null && v1 != null) (v1 - v0).coerceAtLeast(1f) else 10f
    } else 10f

    val path = Path()
    var started = false

    data.forEachIndexed { i, (label, _) ->
        val xCenter = m.getX(i)
        val value = label.toFloatOrNull() ?: return@forEachIndexed

        // PDF formula: (1 / (std * sqrt(2pi))) * e^(-0.5 * ((x-u)/std)^2)
        val z = (value - mean) / stdDev
        val pdf = (1f / (stdDev * kotlin.math.sqrt(2 * kotlin.math.PI))) * kotlin.math.exp(-0.5 * z * z)
        
        // Scale to chart: count = total * width * pdf
        val predictedCount = totalCount * bucketWidth * pdf
        val y = TOP_PADDING_PX + m.drawHeight - m.getHeight(predictedCount.toInt(), max)

        if (!started) {
            path.moveTo(xCenter, y)
            started = true
        } else {
            // Bezier or simple line? Simple line is safer for now.
            path.lineTo(xCenter, y)
        }
    }

    drawPath(
        path = path,
        color = c.normalLine,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 5.5f, // Aumentado para melhor visibilidade
             pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 8f), 0f) // Padrão mais distinto
        )
    )

    // Label da linha (canto superior direito)
    drawContext.canvas.nativeCanvas.drawText(
        "Distribuição Normal",
        size.width - 200f,
        TOP_PADDING_PX + 20f,
        Paint().apply {
            isAntiAlias = true
            color = c.normalLine.toArgb()
            textSize = density.run { 11.sp.toPx() }
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    )
}
