package com.cebolao.lotofacil.ui.components.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Error state card that displays user-friendly error messages with optional retry action.
 * Implements Material Design 3 error state patterns with proper accessibility support.
 *
 * @param error The error to display
 * @param onRetry Optional retry callback - shows retry button when provided
 * @param modifier Modifier for the card
 */
@Composable
fun ErrorStateCard(
    error: AppError,
    onRetry: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    
    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.Spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            // Error icon
            Icon(
                imageVector = when (error) {
                    is AppError.Network -> Icons.Outlined.WifiOff
                    else -> Icons.Outlined.ErrorOutline
                },
                contentDescription = null,
                modifier = Modifier.size(Dimen.IconLarge * 2),
                tint = scheme.error
            )
            
            // Error title
            Text(
                text = stringResource(error.toTitleRes()),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Error message
            Text(
                text = stringResource(error.toMessageRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Retry button (if callback provided)
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = Dimen.Spacing8)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(Dimen.IconSmall)
                    )
                    Spacer(Modifier.width(Dimen.Spacing8))
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}

/**
 * Maps AppError types to user-friendly title string resources.
 */
private fun AppError.toTitleRes(): Int = when (this) {
    is AppError.Network -> R.string.error_network_title
    is AppError.Database -> R.string.error_database_title
    is AppError.Validation -> R.string.error_validation_title
    is AppError.NotFound -> R.string.error_unknown_title
    is AppError.Unknown -> R.string.error_unknown_title
}

/**
 * Maps AppError types to user-friendly message string resources.
 */
private fun AppError.toMessageRes(): Int = when (this) {
    is AppError.Network -> R.string.error_network_message
    is AppError.Database -> R.string.error_database_message
    is AppError.Validation -> R.string.error_validation_message
    is AppError.NotFound -> R.string.error_unknown_message
    is AppError.Unknown -> R.string.error_unknown_message
}

@Preview(showBackground = true)
@Composable
internal fun ErrorStateCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            ErrorStateCard(
                error = AppError.Network(Throwable("Network error")),
                onRetry = {}
            )
        }
    }
}
