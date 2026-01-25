package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Botão de ação primária do aplicativo.
 *
 * Segue o estilo flat moderno, sem sombra, com feedback tátil de escala.
 *
 * @param text Texto do botão
 * @param onClick Ação ao clicar
 * @param modifier Modifier padrão
 * @param enabled Se o botão está habilitado
 * @param isLoading Se está carregando (mostra spinner)
 * @param icon Ícone opcional à esquerda do texto
 * @param isFullWidth Se deve ocupar toda a largura (fillMaxWidth)
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    isFullWidth: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.Offset.PRESSSCALE else 1f,
        animationSpec = Motion.Spring.gentle(),
        label = "buttonScale"
    )

    val widthModifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier
    val loadingDescription = stringResource(R.string.general_loading)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(Dimen.ActionButtonHeightLarge)
            .scale(scale)
            .then(widthModifier)
            .semantics {
                if (isLoading) stateDescription = loadingDescription
            },
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Dimen.Elevation.None,
            pressedElevation = Dimen.Elevation.None,
            focusedElevation = Dimen.Elevation.None,
            hoveredElevation = Dimen.Elevation.None
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = Motion.Tween.fast()) togetherWith
                    fadeOut(animationSpec = Motion.Tween.fast())
            },
            label = "PrimaryButtonContent"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.IconSmall),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Dimen.Border.Thick
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = text,
                            modifier = Modifier.size(Dimen.IconSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimen.Spacing8))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun PrimaryActionButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            PrimaryActionButton(text = "Ação Principal", onClick = {})
            PrimaryActionButton(
                text = "Com Ícone",
                onClick = {},
                icon = Icons.Default.Add
            )
            PrimaryActionButton(text = "Carregando", onClick = {}, isLoading = true)
            PrimaryActionButton(text = "Desabilitado", onClick = {}, enabled = false)
        }
    }
}
