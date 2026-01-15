package com.cebolao.lotofacil.ui.components.feedback

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Estados de feedback para animações
 */
enum class FeedbackType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    LOADING
}

/**
 * Componente de feedback animado compacto e otimizado
 */
@Composable
fun AnimatedFeedback(
    type: FeedbackType,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onDismiss: (() -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (type == FeedbackType.SUCCESS || type == FeedbackType.ERROR) 1.1f else 1f,
        animationSpec = tween(300),
        label = "feedbackScale"
    )

    val (feedbackIcon, color) = when (type) {
        FeedbackType.SUCCESS -> (icon ?: AppIcons.Success) to MaterialTheme.colorScheme.primary
        FeedbackType.ERROR -> (icon ?: AppIcons.Error) to MaterialTheme.colorScheme.error
        FeedbackType.WARNING -> (icon ?: AppIcons.Warning) to MaterialTheme.colorScheme.tertiary
        FeedbackType.INFO -> (icon ?: AppIcons.Info) to MaterialTheme.colorScheme.secondary
        FeedbackType.LOADING -> null to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimen.SpacingShort),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                FeedbackType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                FeedbackType.ERROR -> MaterialTheme.colorScheme.errorContainer
                FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                FeedbackType.INFO -> MaterialTheme.colorScheme.secondaryContainer
                FeedbackType.LOADING -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.SpacingShort),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            // Ícone animado
            Box(
                modifier = Modifier.scale(scale),
                contentAlignment = Alignment.Center
            ) {
                when (type) {
                    FeedbackType.LOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.IconMedium),
                            strokeWidth = 2.dp,
                            color = color
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = feedbackIcon!!,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(Dimen.IconMedium)
                        )
                    }
                }
            }

            // Mensagem
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = when (type) {
                    FeedbackType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                    FeedbackType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    FeedbackType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                    FeedbackType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                    FeedbackType.LOADING -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )

            // Botão de dismiss (opcional)
            onDismiss?.let { dismiss ->
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier.size(Dimen.IconSmall)
                ) {
                    Icon(
                        imageVector = AppIcons.CloseOutlined,
                        contentDescription = "Dismiss",
                        tint = when (type) {
                            FeedbackType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                            FeedbackType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            FeedbackType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                            FeedbackType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                            FeedbackType.LOADING -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Feedback simplificado para mensagens rápidas
 */
@Composable
fun QuickFeedback(
    type: FeedbackType,
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedFeedback(
        type = type,
        message = message,
        modifier = modifier
    )
}
