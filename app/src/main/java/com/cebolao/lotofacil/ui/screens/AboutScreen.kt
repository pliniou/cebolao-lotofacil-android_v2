package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.cebolao.lotofacil.ui.components.game.GameRulesCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.stats.FormattedText
import com.cebolao.lotofacil.ui.components.layout.SectionHeader
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.stats.StatisticsExplanationCard
import com.cebolao.lotofacil.ui.components.common.StudioHero
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.viewmodels.MainViewModel

// Constants for URLs
private const val URL_CAIXA = "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx"
private const val URL_TERMS = ""
private const val URL_PRIVACY = ""

@Composable
fun AboutScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentPalette by viewModel.accentPalette.collectAsStateWithLifecycle()

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    AppScreen(
        title = stringResource(R.string.about_title),
        subtitle = stringResource(R.string.about_subtitle)
    ) { padding ->
        StandardPageLayout(scaffoldPadding = padding) {
            item(key = "hero") {
                StudioHero()
            }

            item(key = "theme_settings") {
                SectionHeader(stringResource(R.string.settings_appearance_title))
                ThemeSettingsCard(
                    currentTheme = themeMode,
                    currentAccent = accentPalette.name.lowercase(),
                    onThemeChange = viewModel::setThemeMode,
                    onAccentChange = { paletteName ->
                        AccentPalette.entries.find {
                            it.name.equals(paletteName, ignoreCase = true)
                        }?.let(viewModel::setAccentPalette)
                    }
                )
            }

            item(key = "resources") {
                SectionHeader(stringResource(R.string.about_resources_title))
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
                ) {
                    ProbabilityCard()
                    CaixaCard { openUrl(URL_CAIXA) }
                }
            }

            item(key = "learn_more") {
                SectionHeader(stringResource(R.string.about_learn_more_title))
                StatisticsExplanationCard()
                GameRulesCard()
            }

            item(key = "legal") {
                SectionHeader(stringResource(R.string.about_legal_title))
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    outlined = false,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentPadding = Dimen.CardContentPadding
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
                    ) {
                        // Fonte de dados
                        InfoRow(
                            icon = AppIcons.Info,
                            title = stringResource(R.string.about_source_title),
                            description = stringResource(R.string.about_source_desc)
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                            thickness = Dimen.Border.Hairline
                        )

                        // Disclaimer
                        InfoRow(
                            icon = AppIcons.Error,
                            title = stringResource(R.string.about_important_title),
                            description = stringResource(R.string.about_important_desc)
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                            thickness = Dimen.Border.Hairline
                        )

                        // Privacidade
                        InfoRow(
                            icon = AppIcons.InfoOutlined,
                            title = stringResource(R.string.about_privacy_title),
                            description = stringResource(R.string.about_privacy_desc)
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                            thickness = Dimen.Border.Hairline
                        )

                        // Versão
                        InfoRow(
                            icon = AppIcons.Info,
                            title = stringResource(R.string.about_version_title),
                            description = com.cebolao.lotofacil.BuildConfig.VERSION_NAME
                        )

                        // Links (se existirem)
                        if (URL_TERMS.isNotBlank() || URL_PRIVACY.isNotBlank()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                                thickness = Dimen.Border.Hairline
                            )

                            if (URL_TERMS.isNotBlank()) {
                                AboutItemRow(
                                    icon = AppIcons.Info,
                                    text = stringResource(R.string.about_terms)
                                ) { openUrl(URL_TERMS) }
                            }

                            if (URL_PRIVACY.isNotBlank()) {
                                AboutItemRow(
                                    icon = AppIcons.Info,
                                    text = stringResource(R.string.about_privacy_policy)
                                ) { openUrl(URL_PRIVACY) }
                            }
                        }
                    }
                }
            }

            item(key = "disclaimer") {
                Spacer(Modifier.height(Dimen.SpacingMedium))
                FormattedText(
                    text = stringResource(R.string.about_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.ScreenPadding)
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingsCard(
    currentTheme: String,
    currentAccent: String,
    onThemeChange: (String) -> Unit,
    onAccentChange: (String) -> Unit
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
                onThemeChange = onThemeChange
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER),
                thickness = Dimen.Border.Hairline
            )

            AccentPaletteSection(
                currentAccent = currentAccent,
                onAccentChange = onAccentChange
            )
        }
    }
}

@Composable
private fun ThemeModeSection(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
        SettingsSectionHeader(
            icon = AppIcons.Tune,
            title = stringResource(R.string.settings_theme_mode_title)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            ThemeOptionButton(
                icon = AppIcons.StarFilled, // Represents Light usually or Sun
                label = stringResource(R.string.settings_theme_light),
                isSelected = currentTheme == THEME_MODE_LIGHT,
                onClick = { onThemeChange(THEME_MODE_LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeOptionButton(
                icon = AppIcons.StarOutlined, // Represents Dark usually or Moon
                label = stringResource(R.string.settings_theme_dark),
                isSelected = currentTheme == THEME_MODE_DARK,
                onClick = { onThemeChange(THEME_MODE_DARK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOptionButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = Motion.Spring.gentle(),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) scheme.primaryContainer else scheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.dp else Dimen.Border.Hairline,
            color = if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = Dimen.Spacing12, horizontal = Dimen.Spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) scheme.primary else scheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
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
            title = stringResource(R.string.settings_accent_color_title)
        )

        // 6 opções em grid de 2 linhas (3 + 3), evitando apertar rótulos.
        AccentPalette.entries
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
        AccentPalette.AZUL -> stringResource(R.string.settings_accent_blue)
        AccentPalette.ROXO -> stringResource(R.string.settings_accent_purple)
        AccentPalette.VERDE -> stringResource(R.string.settings_accent_green)
        AccentPalette.AMARELO -> stringResource(R.string.settings_accent_yellow)
        AccentPalette.ROSA -> stringResource(R.string.settings_accent_pink)
        AccentPalette.LARANJA -> stringResource(R.string.settings_accent_orange)
    }

    // Contraste automático para seeds muito claras (ex.: amarelo)
    val onSeed = if (palette.seed.luminance() > 0.62f) Color(0xFF101114) else Color.White

    val scale by animateFloatAsState(
        targetValue = if (isSelected) Motion.Offset.SELECTSCALE else 1f,
        animationSpec = Motion.Spring.gentle(),
        label = "accentScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(Dimen.ActionButtonHeight)
            .scale(scale),
        color = palette.seed,
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) {
            BorderStroke(2.dp, scheme.primary)
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

@Composable
private fun AboutItemRow(
    icon: ImageVector,
    text: String,
    isClickable: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) { onClick() }
            .padding(horizontal = Dimen.CardContentPadding, vertical = Dimen.SpacingShort),
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
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (isClickable) {
            Icon(
                imageVector = AppIcons.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(Dimen.IconSmall)
            )
        }
    }
}

@Composable
private fun ProbabilityCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        outlined = true,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = Dimen.CardContentPadding
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Icon(
                imageVector = AppIcons.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimen.IconMedium)
            )
            Text(
                text = stringResource(R.string.about_probabilities_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CaixaCard(onClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        outlined = false,
        color = MaterialTheme.colorScheme.primary,
        contentPadding = Dimen.CardContentPadding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Icon(
                imageVector = AppIcons.Home,
                contentDescription = null,
                modifier = Modifier.size(Dimen.IconMedium),
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)) {
                Text(
                    text = stringResource(R.string.about_caixa_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.about_caixa_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f)
                )
            }

            Icon(
                imageVector = AppIcons.Launch,
                contentDescription = stringResource(R.string.open_external_link),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f),
                modifier = Modifier.size(Dimen.IconMedium)
            )
        }
    }
}

@Composable
private fun LearnMoreCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        outlined = true,
        color = MaterialTheme.colorScheme.surface,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)) {
            Text(
                text = stringResource(R.string.about_learn_more_header),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.about_learn_more_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimen.IconMedium)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
