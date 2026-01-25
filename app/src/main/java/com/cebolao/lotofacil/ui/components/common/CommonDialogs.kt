package com.cebolao.lotofacil.ui.components.common

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.haptics.HapticFeedbackManager
import com.cebolao.lotofacil.ui.haptics.HapticFeedbackType
import com.cebolao.lotofacil.ui.haptics.rememberHapticFeedback
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun AppConfirmationDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes confirmText: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Warning,
    @StringRes cancelText: Int = R.string.general_cancel,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.error,
    confirmColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    enableHaptics: Boolean = true
) {
    val haptics = rememberHapticFeedback()
    
    // Animate dialog appearance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else Motion.Offset.SCALE,
        animationSpec = Motion.Spring.gentle(),
        label = "dialog_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = Motion.Tween.enter(),
        label = "dialog_alpha"
    )

    AlertDialog(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
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
            TextButton(
                onClick = {
                    if (enableHaptics) {
                        haptics.performHapticFeedback(HapticFeedbackType.MEDIUM)
                    }
                    onConfirm()
                }
            ) {
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
internal fun AppConfirmationDialogPreview() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)) {
            Text("Dialog Preview Placeholder")
        }
    }
}
