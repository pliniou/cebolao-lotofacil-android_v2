package com.cebolao.lotofacil.ui.components.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.ThemeMode
import com.cebolao.lotofacil.presentation.viewmodel.MainUiEvent
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.AccentAmarelo
import com.cebolao.lotofacil.ui.theme.AccentAzul
import com.cebolao.lotofacil.ui.theme.AccentLaranja
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AccentPalettes
import com.cebolao.lotofacil.ui.theme.AccentRosa
import com.cebolao.lotofacil.ui.theme.AccentRoxo
import com.cebolao.lotofacil.ui.theme.AccentVerde
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.DarkTextPrimary
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.LightTextPrimary
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun ThemeSettingsCard(
    currentTheme: ThemeMode,
    currentAccent: String,
    onEvent: (MainUiEvent) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        outlined = true,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
        ) {
            ThemeModeSection(
                currentTheme = currentTheme,
                onThemeChange = { onEvent(MainUiEvent.SetThemeMode(it)) }
            )

            AppDivider()

            AccentPaletteSection(
                currentAccent = currentAccent,
                onAccentChange = { name ->
                    AccentPalettes.find { it.name.equals(name, ignoreCase = true) }
                        ?.let { onEvent(MainUiEvent.SetAccentPalette(it)) }
                }
            )
        }
    }
}

@Composable
private fun ThemeModeSection(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)) {
        SettingsSectionHeader(
            icon = AppIcons.Tune,
            title = stringResource(R.string.about_theme_mode)
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            ThemeModeOption(
                icon = AppIcons.Settings,
                label = stringResource(R.string.about_theme_system),
                isSelected = currentTheme == ThemeMode.SYSTEM,
                onClick = { onThemeChange(ThemeMode.SYSTEM) }
            )
            ThemeModeOption(
                icon = AppIcons.StarFilled,
                label = stringResource(R.string.about_theme_light),
                isSelected = currentTheme == ThemeMode.LIGHT,
                onClick = { onThemeChange(ThemeMode.LIGHT) }
            )
            ThemeModeOption(
                icon = AppIcons.StarOutlined,
                label = stringResource(R.string.about_theme_dark),
                isSelected = currentTheme == ThemeMode.DARK,
                onClick = { onThemeChange(ThemeMode.DARK) }
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    AppCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.RadioButton
                stateDescription = if (isSelected) label else ""
            },
        outlined = !isSelected,
        color = if (isSelected) scheme.primaryContainer.copy(alpha = 0.55f) else scheme.surface,
        contentPadding = Dimen.ItemSpacing
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimen.IconMedium),
                tint = if (isSelected) scheme.primary else scheme.onSurfaceVariant
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AccentPaletteSection(
    currentAccent: String,
    onAccentChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)) {
        SettingsSectionHeader(
            icon = AppIcons.Tune,
            title = stringResource(R.string.about_accent_color)
        )

        // 6 opções em grid de 2 linhas (3 + 3), evitando apertar rótulos.
        AccentPalettes
            .chunked(3)
            .forEach { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
                ) {
                    chunk.forEach { palette ->
                        AccentColorButton(
                            palette = palette,
                            isSelected = currentAccent.uppercase() == palette.name,
                            onClick = { onAccentChange(palette.name.lowercase()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}

@Composable
private fun AccentColorButton(
    palette: AccentPalette,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val label = when (palette) {
        AccentAzul -> stringResource(R.string.about_accent_blue)
        AccentRoxo -> stringResource(R.string.about_accent_purple)
        AccentVerde -> stringResource(R.string.about_accent_green)
        AccentAmarelo -> stringResource(R.string.about_accent_yellow)
        AccentRosa -> stringResource(R.string.about_accent_pink)
        AccentLaranja -> stringResource(R.string.about_accent_orange)
        else -> stringResource(R.string.about_accent_blue)
    }
    val contentDesc = stringResource(R.string.about_accessibility_select_accent, label)

    // Contraste automático para seeds muito claras (ex.: amarelo)
    val onSeed = if (palette.primary.luminance() > 0.62f) LightTextPrimary else DarkTextPrimary

    val scale by animateFloatAsState(
        targetValue = if (isSelected) Motion.Offset.SELECTSCALE else 1f,
        animationSpec = Motion.Spring.gentle(),
        label = "accentScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(Dimen.ActionButtonHeight)
            .scale(scale)
            .semantics {
                role = Role.Button
                stateDescription = if (isSelected) label else ""
                this.contentDescription = contentDesc
            },
        color = palette.primary,
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) {
            BorderStroke(Dimen.Border.Thick, scheme.primary)
        } else {
            BorderStroke(Dimen.Border.Hairline, scheme.onSurface.copy(alpha = 0.10f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimen.SpacingShort),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = onSeed,
                    modifier = Modifier.size(Dimen.IconSmall)
                )
                Spacer(Modifier.width(Dimen.Spacing4))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = onSeed,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimen.IconSmall)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
