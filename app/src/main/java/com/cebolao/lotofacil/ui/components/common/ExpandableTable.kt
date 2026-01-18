package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Table with expand/collapse functionality for long data sets.
 * 
 * Automatically shows a "See more/less" button when rows exceed initialVisibleRows.
 * Uses Motion tokens for smooth, consistent animations.
 * 
 * @param data Table content and structure
 * @param modifier Modifier for the container
 * @param initialVisibleRows Number of rows to show when collapsed (default: 5)
 * @param style Visual styling options
 */
@Composable
fun ExpandableTable(
    data: AppTableData,
    modifier: Modifier = Modifier,
    initialVisibleRows: Int = 5,
    style: AppTableStyle = AppTableStyle()
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    
    val hasMore = data.rows.size > initialVisibleRows
    val visibleRows = if (expanded || !hasMore) data.rows else data.rows.take(initialVisibleRows)
    
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = Motion.Spring.gentle(),
        label = "expand_icon_rotation"
    )

    Column(modifier = modifier) {
        // Main table with visible rows
        AppTable(
            data = data.copy(rows = visibleRows),
            style = style
        )
        
        // Expand/collapse button
        if (hasMore) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top =  Dimen.Spacing8),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Text(
                            text = stringResource(
                                if (expanded) R.string.table_see_less 
                                else R.string.table_see_more
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(Modifier.width(Dimen.Spacing4))
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier
                                .size(Dimen.IconSmall)
                                .rotate(iconRotation)
                        )
                    }
                    
                    if (!expanded) {
                        Spacer(Modifier.width(Dimen.Spacing8))
                        Text(
                            text = stringResource(R.string.and_more_count, data.rows.size - initialVisibleRows),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
