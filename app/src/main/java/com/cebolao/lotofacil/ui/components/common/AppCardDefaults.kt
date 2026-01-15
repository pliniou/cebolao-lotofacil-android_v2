package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen

object AppCardDefaults {
    val DEFAULT_ELEVATION = 0.dp
    val DEFAULT_ROUNDED_CORNER_RADIUS = 16.dp
}

@Composable
fun AppCardDefaults(
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    color: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: androidx.compose.ui.unit.Dp = AppCardDefaults.DEFAULT_ELEVATION,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        tonalElevation = tonalElevation,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        border = if (outlined) {
            androidx.compose.foundation.BorderStroke(
                width = Dimen.Border.Thin,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
            )
        } else null
    ) {
        content()
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.general_loading)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(Dimen.IconMedium),
            strokeWidth = Dimen.Border.Thin
        )
        Spacer(Modifier.width(Dimen.ItemSpacing))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    AppCardDefaults(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (actionLabel != null && onActionClick != null) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(top = Dimen.Spacing16)
                ) {
                    androidx.compose.material3.Button(
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}
