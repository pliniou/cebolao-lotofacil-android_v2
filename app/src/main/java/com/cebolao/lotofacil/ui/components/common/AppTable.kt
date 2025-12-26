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

@Composable
fun AppTable(
    headers: List<String>,
    rows: List<List<String>>,
    weights: List<Float>,
    textAligns: List<TextAlign>,
    modifier: Modifier = Modifier,
    showDividers: Boolean = false,
    headerStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall,
    rowStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            headers.forEachIndexed { index, title ->
                Text(
                    text = title,
                    modifier = Modifier.weight(weights.getOrElse(index) { 1f }),
                    textAlign = textAligns.getOrElse(index) { TextAlign.Start },
                    fontWeight = FontWeight.Bold,
                    style = headerStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Rows
        rows.forEachIndexed { rowIndex, row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimen.Spacing4)
            ) {
                row.forEachIndexed { colIndex, cell ->
                    Text(
                        text = cell,
                        modifier = Modifier.weight(weights.getOrElse(colIndex) { 1f }),
                        textAlign = textAligns.getOrElse(colIndex) { TextAlign.Start },
                        style = rowStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (showDividers && rowIndex < rows.lastIndex) {
                 HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                    thickness = Dimen.Border.Hairline
                )
            }
        }
    }
}
