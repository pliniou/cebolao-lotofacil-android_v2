package com.cebolao.lotofacil.ui.components.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun AppConfirmationDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes confirmText: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier, // Modifier moved to first optional parameter
    icon: ImageVector = Icons.Default.Warning,
    @StringRes cancelText: Int = R.string.general_cancel,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.error,
    confirmColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimen.IconLarge),
                tint = iconTint
            )
        },
        title = {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(confirmText),
                    style = MaterialTheme.typography.labelLarge,
                    color = confirmColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(cancelText),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        // Alinhado ao InfoDialog para consistência visual
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun AppConfirmationDialogPreview() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Note: Alerts are tricky to preview inline, but we can check compilation
            // and structure. In a real preview environment, this might need a surface.
             Text("Dialog Preview Placeholder")
        }
    }
}
