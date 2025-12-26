package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.GlassSurfaceDark
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Variantes visuais para AppCard seguindo princípios de flat design.
 */
enum class CardVariant {
    /** Card sólido com background opaco (padrão) */
    Solid,
    /** Card com efeito glassmorphism (semi-transparente) */
    Glass,
    /** Card apenas com borda, sem background destacado */
    Outlined,
    /** Card com elevação sutil para hierarquia */
    Elevated
}

/**
 * Scope para conteúdo do AppCard com acesso ao nesting level.
 */
interface CardScope : ColumnScope {
    val nestingLevel: Int
}

/** Card nesting level tracker for recursive styling. */
val LocalCardNesting = compositionLocalOf { 0 }

/**
 * AppCard Unificado.
 * Combina funcionalidade do antigo AppCard e recursos visuais do ModernCard.
 * 
 * @param outlined Compatibilidade retroativa. Se true, força variant = Outlined.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Solid,
    outlined: Boolean = false,
    color: Color = Color.Unspecified, // Se Unspecified, usa lógica interna baseada na variante
    hasBorder: Boolean = true,
    elevation: Dp = Dimen.Elevation.None,
    contentPadding: Dp = Dimen.CardContentPadding,
    title: String? = null,
    headerActions: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    header: (@Composable CardScope.() -> Unit)? = null,
    footer: (@Composable CardScope.() -> Unit)? = null,
    content: @Composable CardScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nestingLevel = LocalCardNesting.current

    val shape = MaterialTheme.shapes.large

    // Retro-compatibility logic
    val effectiveVariant = if (outlined) CardVariant.Outlined else when (nestingLevel) {
        0 -> variant
        1 -> CardVariant.Outlined
        else -> CardVariant.Solid
    }

    // Interaction states
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) Motion.Offset.PRESSSCALE else 1f,
        animationSpec = Motion.Spring.snappy(),
        label = "cardScale"
    )

    // Background determination
    val defaultBg = when (effectiveVariant) {
        CardVariant.Glass -> GlassSurfaceDark
        CardVariant.Outlined -> scheme.surface
        CardVariant.Solid -> scheme.surfaceContainer
        CardVariant.Elevated -> scheme.surfaceContainerHigh
    }
    
    val effectiveBackground = if (color != Color.Unspecified) color else defaultBg

    // Border determination
    val border = if (hasBorder || effectiveVariant == CardVariant.Outlined) {
        BorderStroke(
            width = when (effectiveVariant) {
                CardVariant.Glass -> Dimen.Glass.BorderWidth
                CardVariant.Outlined -> Dimen.Border.Thin
                CardVariant.Solid -> Dimen.Border.Hairline
                CardVariant.Elevated -> Dimen.Border.Hairline
            },
            color = scheme.outlineVariant.copy(
                alpha = when (effectiveVariant) {
                    CardVariant.Glass -> Alpha.DIVIDER
                    CardVariant.Outlined -> Alpha.MEDIUM
                    CardVariant.Solid -> 0.35f
                    CardVariant.Elevated -> 0.30f
                }
            )
        )
    } else null

    // Elevation logic
    val effectiveElevation = when (effectiveVariant) {
        CardVariant.Elevated -> Dimen.Elevation.Level1
        else -> elevation
    }

    val colors = CardDefaults.cardColors(containerColor = effectiveBackground)
    
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = effectiveElevation,
        pressedElevation = if (onClick != null && effectiveVariant == CardVariant.Elevated) Dimen.Elevation.Level2 else effectiveElevation
    )

    val cardModifier = if (onClick != null) modifier.scale(scale) else modifier

    CompositionLocalProvider(LocalCardNesting provides nestingLevel + 1) {
        if (onClick != null) {
            Card(
                onClick = onClick,
                modifier = cardModifier,
                shape = shape,
                colors = colors,
                elevation = cardElevation,
                border = border,
                interactionSource = interactionSource
            ) {
                CardContent(contentPadding, nestingLevel + 1, title, headerActions, header, footer, content)
            }
        } else {
            Card(
                modifier = cardModifier,
                shape = shape,
                colors = colors,
                elevation = cardElevation,
                border = border
            ) {
                CardContent(contentPadding, nestingLevel + 1, title, headerActions, header, footer, content)
            }
        }
    }
}

@Composable
private fun CardContent(
    contentPadding: Dp,
    nestingLevel: Int,
    title: String?,
    headerActions: (@Composable RowScope.() -> Unit)?,
    header: (@Composable CardScope.() -> Unit)?,
    footer: (@Composable CardScope.() -> Unit)?,
    content: @Composable CardScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
    ) {
        val scope = object : CardScope, ColumnScope by this {
            override val nestingLevel: Int = nestingLevel
        }

        if (title != null || headerActions != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimen.SmallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (headerActions != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        headerActions()
                    }
                }
            }
        }

        header?.let { it(scope) }
        content(scope)
        footer?.let { it(scope) }
    }
}
