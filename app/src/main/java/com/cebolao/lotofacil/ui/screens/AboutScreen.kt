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
import com.cebolao.lotofacil.ui.components.game.GameRulesCard
import com.cebolao.lotofacil.ui.components.layout.SectionHeader
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.stats.StatisticsExplanationCard
import com.cebolao.lotofacil.ui.components.common.StudioHero
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel

import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.common.StandardInfoRow
import com.cebolao.lotofacil.presentation.viewmodel.MainUiEvent

// Constants for URLs
private const val URL_CAIXA = "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx"
private const val URL_TERMS = ""
private const val URL_PRIVACY = ""

@Composable
fun AboutScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentPalette by viewModel.accentPalette.collectAsStateWithLifecycle()
    
    AboutScreenContent(
        themeMode = themeMode,
        accentPalette = accentPalette,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun AboutScreenContent(
    themeMode: String,
    accentPalette: AccentPalette,
    onEvent: (MainUiEvent) -> Unit
) {
    val context = LocalContext.current

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
                SectionHeader("Aparência")
                ThemeSettingsCard(
                    currentTheme = themeMode,
                    currentAccent = accentPalette.name.lowercase(),
                    onEvent = onEvent
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
                SectionHeader("Saiba Mais")
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
                        StandardInfoRow(
                            icon = AppIcons.Info,
                            title = "Fonte dos Dados",
                            description = "Resultados oficiais da Lotofácil via API pública. Atualização manual via pull-to-refresh."
                        )

                        AppDivider()

                        // Disclaimer
                        StandardInfoRow(
                            icon = AppIcons.Error,
                            title = "Importante",
                            description = "Este app organiza informações públicas. Não aumenta suas chances de ganhar, apenas evita combinações estatisticamente improváveis."
                        )

                        AppDivider()

                        // Privacidade
                        StandardInfoRow(
                            icon = AppIcons.InfoOutlined,
                            title = "Privacidade",
                            description = "Nenhum dado pessoal é coletado. Jogos ficam salvos apenas localmente no seu dispositivo."
                        )

                        AppDivider()

                        // Versão
                        StandardInfoRow(
                            icon = AppIcons.Info,
                            title = "Versão",
                            description = com.cebolao.lotofacil.BuildConfig.VERSION_NAME
                        )

                        // Links (se existirem)
                        if (URL_TERMS.isNotBlank() || URL_PRIVACY.isNotBlank()) {
                            AppDivider()

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
                    AccentPalette.entries.find { it.name.equals(name, ignoreCase = true) }
                        ?.let { onEvent(MainUiEvent.SetAccentPalette(it)) }
                }
            )
        }
    }
}

@Composable
private fun ThemeModeSection(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)) {
        SettingsSectionHeader(
            icon = AppIcons.Tune,
            title = "Modo de Tema"
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            ThemeModeOption(
                icon = AppIcons.StarFilled,
                label = "Claro",
                isSelected = currentTheme == THEME_MODE_LIGHT,
                onClick = { onThemeChange(THEME_MODE_LIGHT) }
            )
            ThemeModeOption(
                icon = AppIcons.StarOutlined,
                label = "Escuro",
                isSelected = currentTheme == THEME_MODE_DARK,
                onClick = { onThemeChange(THEME_MODE_DARK) }
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
        modifier = Modifier.fillMaxWidth(),
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
            title = "Cor de Destaque"
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
        AccentPalette.AZUL -> "Azul"
        AccentPalette.ROXO -> "Roxo"
        AccentPalette.VERDE -> "Verde"
        AccentPalette.AMARELO -> "Amarelo"
        AccentPalette.ROSA -> "Rosa"
        AccentPalette.LARANJA -> "Laranja"
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
