package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen

data class AppTableData(
    val headers: List<String>,
    val rows: List<List<String>>,
    val weights: List<Float>,
    val textAligns: List<TextAlign>
)

data class AppTableStyle(
    val showDividers: Boolean = false,
    val headerStyle: androidx.compose.ui.text.TextStyle? = null,
    val rowStyle: androidx.compose.ui.text.TextStyle? = null
)

@Composable
fun AppTable(
    data: AppTableData,
    modifier: Modifier = Modifier,
    style: AppTableStyle = AppTableStyle()
) {
    val headerStyle = style.headerStyle ?: MaterialTheme.typography.labelSmall

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            data.headers.forEachIndexed { index, title ->
                Text(
                    text = title,
                    modifier = Modifier.weight(data.weights.getOrElse(index) { 1f }),
                    textAlign = data.textAligns.getOrElse(index) { TextAlign.Start },
                    fontWeight = FontWeight.Bold,
                    style = headerStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Rows
        data.rows.forEachIndexed { rowIndex, row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimen.Spacing4)
            ) {
                row.forEachIndexed { colIndex, cell ->
                    style.rowStyle?.let {
                        Text(
                            text = cell,
                            modifier = Modifier.weight(data.weights.getOrElse(colIndex) { 1f }),
                            textAlign = data.textAligns.getOrElse(colIndex) { TextAlign.Start },
                            style = it,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (style.showDividers && rowIndex < data.rows.lastIndex) {
                 HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                    thickness = Dimen.Border.Hairline
                )
            }
        }
    }
}
