package com.cebolao.lotofacil.ui.components.layout

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.GlassSurfaceDark
import com.cebolao.lotofacil.ui.theme.GlassSurfaceLight
import com.cebolao.lotofacil.ui.theme.Motion

enum class CardVariant {
    Solid,
    Glass,
    Outlined,
    Elevated // Added back to avoid breaking potential usages, mapped to Solid
}

interface CardScope : ColumnScope {
    val nestingLevel: Int
}

val LocalCardNesting = compositionLocalOf { 0 }

@Stable
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Solid,
    outlined: Boolean = false,
    color: Color = Color.Unspecified,
    hasBorder: Boolean = true,
    contentPadding: Dp = Dimen.CardContentPadding,
    title: String? = null,
    headerActions: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    header: (@Composable CardScope.() -> Unit)? = null,
    footer: (@Composable CardScope.() -> Unit)? = null,
    content: @Composable CardScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    val nestingLevel = LocalCardNesting.current
    val shape = MaterialTheme.shapes.medium // Keeping consistent shape

    // Variant logic
    val effectiveVariant = if (outlined) CardVariant.Outlined else when (nestingLevel) {
        0 -> if (variant == CardVariant.Elevated) CardVariant.Solid else variant
        1 -> CardVariant.Outlined
        else -> CardVariant.Solid
    }

    // Interaction
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) Motion.Offset.PRESSSCALE else 1f,
        animationSpec = Motion.Spring.snappy(),
        label = "cardScale"
    )

    // Background (memoized)
    val effectiveBackground = remember(color, effectiveVariant, isDark, scheme.surface, scheme.surfaceContainer) {
        when {
            color != Color.Unspecified -> color
            effectiveVariant == CardVariant.Glass -> if (isDark) GlassSurfaceDark else GlassSurfaceLight
            effectiveVariant == CardVariant.Outlined -> Color.Transparent
            nestingLevel == 0 -> scheme.surface
            else -> scheme.surfaceContainer
        }
    }

    // Border (memoized)
    val border = remember(hasBorder, effectiveVariant, scheme.outlineVariant) {
        if (hasBorder || effectiveVariant == CardVariant.Outlined) {
            BorderStroke(
                width = Dimen.Border.Thin,
                color = if (effectiveVariant == CardVariant.Glass) {
                    scheme.outlineVariant.copy(alpha = 0.2f)
                } else {
                    scheme.outlineVariant // Using the new semantic outline variant
                }
            )
        } else null
    }

    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = Dimen.Elevation.None,
        pressedElevation = Dimen.Elevation.None,
        focusedElevation = Dimen.Elevation.None,
        hoveredElevation = Dimen.Elevation.None,
        draggedElevation = Dimen.Elevation.None
    )
    val colors = CardDefaults.cardColors(containerColor = effectiveBackground)

    val baseModifier = remember(onClick, modifier, scale) {
        if (onClick != null) {
            modifier
                .scale(scale)
                .semantics { this.role = Role.Button }
        } else modifier
    }

    CompositionLocalProvider(LocalCardNesting provides nestingLevel + 1) {
        val cardContent: @Composable ColumnScope.() -> Unit = {
            val scope = remember(nestingLevel) {
                object : CardScope, ColumnScope by this {
                    override val nestingLevel: Int = nestingLevel
                }
            }
            
            Column(
                modifier = Modifier.padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
            ) {
                 if (title != null || headerActions != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (contentPadding > 0.dp) Dimen.Spacing8 else 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (headerActions != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
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

        if (onClick != null) {
            Card(
                onClick = onClick,
                modifier = baseModifier,
                shape = shape,
                colors = colors,
                elevation = cardElevation,
                border = border,
                interactionSource = interactionSource,
                content = cardContent
            )
        } else {
            Card(
                modifier = baseModifier,
                shape = shape,
                colors = colors,
                elevation = cardElevation,
                border = border,
                content = cardContent
            )
        }
    }
}
