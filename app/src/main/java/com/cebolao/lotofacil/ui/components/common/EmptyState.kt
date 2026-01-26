package com.cebolao.lotofacil.ui.components.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Standardized empty state component.
 * Displays an icon, title, description and optional action when no content is available.
 *
 * @param title The primary message (e.g. "No games found")
 * @param description Detailed explanation or instruction
 * @param modifier Modifier for the container
 * @param icon The icon to display above the text
 * @param action Optional composable block for call-to-action button
 */
@Composable
fun EmptyState(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimen.Spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimen.IconLarge * 2),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Dimen.Spacing16))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        if (description != null) {
            Spacer(modifier = Modifier.height(Dimen.Spacing4))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        if (action != null) {
            Spacer(modifier = Modifier.height(Dimen.Spacing24))
            action()
        }
    }
}

/**
 * Empty state overload using String resources.
 */
@Composable
fun EmptyState(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
    action: (@Composable () -> Unit)? = null
) {
    EmptyState(
        title = stringResource(titleRes),
        description = descriptionRes?.let { stringResource(it) },
        modifier = modifier,
        icon = icon,
        action = action
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState(
            title = "No Internet Connection",
            description = "Please check your network settings and try again."
        )
    }
}
