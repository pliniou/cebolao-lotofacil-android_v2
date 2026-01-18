package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Data class for table content.
 * 
 * @param headers Column header labels
 * @param rows Data rows (outer list = rows, inner list = cells)
 * @param weights Column width weights (must match header count)
 * @param textAligns Text alignment per column (must match header count)
 * @param emptyStateText Optional message when rows are empty
 */
@Immutable
data class AppTableData(
    val headers: List<String>,
    val rows: List<List<String>>,
    val weights: List<Float>,
    val textAligns: List<TextAlign>,
    val emptyStateText: String? = null
)

/**
 * Styling options for table rendering.
 * 
 * @param showDividers Show horizontal dividers between rows
 * @param zebraRows Alternate row backgrounds for better readability
 * @param compactMode Use tighter spacing (40dp vs 48dp row height)
 * @param headerStyle Custom typography for headers (default: labelMedium + SemiBold)
 * @param rowStyle Custom typography for rows (default: bodyMedium)
 * @param showEmptyPlaceholder Show placeholder row when data is empty
 */
@Immutable
data class AppTableStyle(
    val showDividers: Boolean = false,
    val zebraRows: Boolean = false,
    val compactMode: Boolean = false,
    val headerStyle: TextStyle? = null,
    val rowStyle: TextStyle? = null,
    val showEmptyPlaceholder: Boolean = true
)

/**
 * Unified table component with consistent typography, spacing, alignment and theming.
 * 
 * Features:
 * - Semantic header styling (labelMedium + SemiBold)
 * - Consistent row height (48dp standard, 40dp compact)
 * - Theme-aware zebra rows
 * - Empty state placeholder
 * - Proper text overflow handling
 * - Accessibility support
 * 
 * @param data Table content and structure
 * @param modifier Modifier for the table container
 * @param style Visual styling options
 */
@Composable
fun AppTable(
    data: AppTableData,
    modifier: Modifier = Modifier,
    style: AppTableStyle = AppTableStyle()
) {
    val scheme = MaterialTheme.colorScheme
    val headerStyle = style.headerStyle ?: MaterialTheme.typography.labelMedium
    val rowStyle = style.rowStyle ?: MaterialTheme.typography.bodyMedium
    val rowPaddingVertical = if (style.compactMode) Dimen.Spacing8 else Dimen.Spacing12
    val rowPaddingHorizontal = Dimen.Spacing12

    Column(modifier.fillMaxWidth()) {
        // Header row
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimen.Spacing8, horizontal = rowPaddingHorizontal)
                .semantics { heading() }
        ) {
            data.headers.forEachIndexed { index, title ->
                Text(
                    text = title,
                    modifier = Modifier.weight(data.weights.getOrElse(index) { 1f }),
                    textAlign = data.textAligns.getOrElse(index) { TextAlign.Start },
                    fontWeight = FontWeight.SemiBold,
                    style = headerStyle,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Data rows or empty state
        if (data.rows.isEmpty() && style.showEmptyPlaceholder) {
            TableEmptyState(
                message = data.emptyStateText ?: stringResource(R.string.table_empty_state),
                modifier = Modifier.padding(vertical = rowPaddingVertical, horizontal = rowPaddingHorizontal)
            )
        } else {
            data.rows.forEachIndexed { rowIndex, row ->
                val backgroundColor = if (style.zebraRows && rowIndex % 2 == 1) {
                    scheme.surfaceContainerLowest
                } else {
                    scheme.surface
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = rowPaddingVertical, horizontal = rowPaddingHorizontal)
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        Text(
                            text = cell,
                            modifier = Modifier.weight(data.weights.getOrElse(colIndex) { 1f }),
                            textAlign = data.textAligns.getOrElse(colIndex) { TextAlign.Start },
                            style = rowStyle,
                            color = scheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (style.showDividers && rowIndex < data.rows.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant.copy(alpha = Alpha.DIVIDER_SUBTLE),
                        thickness = Dimen.Border.Hairline
                    )
                }
            }
        }
    }
}

/**
 * Empty state placeholder for tables with no data.
 */
@Composable
private fun TableEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimen.Spacing16),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = Dimen.Spacing8)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
