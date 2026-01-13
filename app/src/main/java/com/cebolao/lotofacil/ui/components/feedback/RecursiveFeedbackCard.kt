package com.cebolao.lotofacil.ui.components.feedback

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Estado de feedback visual para operações.
 */
enum class FeedbackState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Componente recursivo para feedback visual com animações suaves.
 * Aplica design flat com Material Design 3.
 */
@Composable
fun RecursiveFeedbackCard(
    state: FeedbackState,
    title: String? = null,
    message: String,
    subMessages: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    nestingLevel: Int = 0
) {
    val scale by animateFloatAsState(
        targetValue = when (state) {
            FeedbackState.SUCCESS, FeedbackState.ERROR -> 1.1f
            else -> 1f
        },
        animationSpec = Motion.Spring.gentle(),
        label = "feedbackScale"
    )

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Feedback principal com ícone e mensagem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
            ) {
                FeedbackIcon(
                    state = state,
                    modifier = Modifier.scale(scale)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
            
            // Sub-mensagens recursivas
            if (subMessages.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
                    modifier = Modifier.padding(start = Dimen.SectionSpacing)
                ) {
                    subMessages.forEach { subMessage ->
                        SubMessageRow(
                            message = subMessage,
                            nestingLevel = nestingLevel + 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente para ícones de feedback com animações.
 */
@Composable
private fun FeedbackIcon(
    state: FeedbackState,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (state) {
        FeedbackState.SUCCESS -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
        FeedbackState.ERROR -> Icons.Filled.Error to MaterialTheme.colorScheme.error
        FeedbackState.WARNING -> Icons.Filled.Warning to MaterialTheme.colorScheme.tertiary
        FeedbackState.INFO -> Icons.Filled.Info to MaterialTheme.colorScheme.secondary
        FeedbackState.LOADING -> null to MaterialTheme.colorScheme.primary
        FeedbackState.IDLE -> null to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AnimatedContent(
        targetState = state,
        transitionSpec = {
            when (targetState) {
                FeedbackState.LOADING -> (
                    (fadeIn(animationSpec = Motion.Tween.enter()) +
                        scaleIn(animationSpec = Motion.Spring.gentle())) togetherWith
                        fadeOut(animationSpec = Motion.Tween.exit())
                )
                else -> (
                    (fadeIn(animationSpec = Motion.Tween.enter()) +
                        scaleIn(animationSpec = Motion.Spring.gentle())) togetherWith
                        (fadeOut(animationSpec = Motion.Tween.exit()) +
                            scaleOut(animationSpec = Motion.Spring.gentle()))
                )
            }
        },
        label = "feedbackIconTransition"
    ) { currentState ->
        when (currentState) {
            FeedbackState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.IconMedium),
                    strokeWidth = Dimen.Border.Thin,
                    color = color
                )
            }
            else -> {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = color,
                        modifier = modifier.size(Dimen.IconMedium)
                    )
                }
            }
        }
    }
}

/**
 * Componente recursivo para sub-mensagens de feedback.
 */
@Composable
private fun SubMessageRow(
    message: String,
    nestingLevel: Int = 0
) {
    val horizontalPadding = Dimen.ItemSpacing + (nestingLevel * Dimen.Spacing4.value).dp
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $message",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (nestingLevel == 0) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Utilitário para construir feedbacks complexos.
 */
object FeedbackBuilder {
    fun buildSuccess(
        message: String,
        title: String? = null,
        vararg subMessages: String
    ): Pair<Triple<FeedbackState, String?, String>, List<String>> = Triple(FeedbackState.SUCCESS, title, message) to subMessages.toList()
    
    fun buildError(
        message: String,
        title: String? = null,
        vararg subMessages: String
    ): Pair<Triple<FeedbackState, String?, String>, List<String>> = Triple(FeedbackState.ERROR, title, message) to subMessages.toList()
    
    fun buildWarning(
        message: String,
        title: String? = null,
        vararg subMessages: String
    ): Pair<Triple<FeedbackState, String?, String>, List<String>> = Triple(FeedbackState.WARNING, title, message) to subMessages.toList()
    
    fun buildInfo(
        message: String,
        title: String? = null,
        vararg subMessages: String
    ): Pair<Triple<FeedbackState, String?, String>, List<String>> = Triple(FeedbackState.INFO, title, message) to subMessages.toList()
}
