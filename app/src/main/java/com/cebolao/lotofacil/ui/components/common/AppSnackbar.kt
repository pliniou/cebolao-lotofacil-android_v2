package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.ErrorBase
import com.cebolao.lotofacil.ui.theme.InfoBase
import com.cebolao.lotofacil.ui.theme.SuccessBase
import com.cebolao.lotofacil.ui.theme.WarningBase

/**
 * Semantic snackbar types for consistent feedback styling
 */
enum class SnackbarType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    NEUTRAL
}

/**
 * Data class to hold snackbar message with its type
 */
data class AppSnackbarData(
    val message: String,
    val type: SnackbarType = SnackbarType.NEUTRAL
)

/**
 * Get icon for snackbar type
 */
private fun SnackbarType.icon(): ImageVector? = when (this) {
    SnackbarType.SUCCESS -> Icons.Default.Check
    SnackbarType.ERROR -> Icons.Default.Error
    SnackbarType.WARNING -> Icons.Default.Warning
    SnackbarType.INFO -> Icons.Default.Info
    SnackbarType.NEUTRAL -> null
}

/**
 * Get container color for snackbar type
 */
@Composable
private fun SnackbarType.containerColor(): Color = when (this) {
    SnackbarType.SUCCESS -> SuccessBase
    SnackbarType.ERROR -> ErrorBase
    SnackbarType.WARNING -> WarningBase
    SnackbarType.INFO -> InfoBase
    SnackbarType.NEUTRAL -> MaterialTheme.colorScheme.inverseSurface
}

/**
 * Get content color for snackbar type
 */
@Composable
private fun SnackbarType.contentColor(): Color = when (this) {
    SnackbarType.SUCCESS, SnackbarType.ERROR, SnackbarType.INFO -> Color.White
    SnackbarType.WARNING -> Color.Black
    SnackbarType.NEUTRAL -> MaterialTheme.colorScheme.inverseOnSurface
}

/**
 * Centralized styled snackbar host with semantic types and animations
 * 
 * Usage:
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 * var snackbarType by remember { mutableStateOf(SnackbarType.NEUTRAL) }
 * 
 * Scaffold(
 *     snackbarHost = {
 *         AppSnackbarHost(
 *             hostState = snackbarHostState,
 *             snackbarType = snackbarType
 *         )
 *     }
 * )
 * 
 * // To show:
 * snackbarType = SnackbarType.SUCCESS
 * snackbarHostState.showSnackbar("Game saved!")
 * ```
 */
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarType: SnackbarType = SnackbarType.NEUTRAL
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            AppSnackbar(
                snackbarData = snackbarData,
                type = snackbarType
            )
        }
    )
}

/**
 * Individual styled snackbar with icon and semantic colors
 */
@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType,
    modifier: Modifier = Modifier
) {
    val icon = type.icon()
    val containerColor = type.containerColor()
    val contentColor = type.contentColor()

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 200)
        ) + fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        Snackbar(
            modifier = modifier.padding(Dimen.Spacing12),
            shape = RoundedCornerShape(12.dp),
            containerColor = containerColor,
            contentColor = contentColor,
            actionContentColor = contentColor,
            dismissActionContentColor = contentColor
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(Dimen.IconSmall),
                        tint = contentColor
                    )
                    Spacer(Modifier.width(Dimen.Spacing8))
                }
                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}
