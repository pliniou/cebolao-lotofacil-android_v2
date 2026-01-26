package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.ThemeMode
import com.cebolao.lotofacil.presentation.viewmodel.MainUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel
import com.cebolao.lotofacil.ui.components.common.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.common.StandardInfoRow
import com.cebolao.lotofacil.ui.components.common.StudioHero
import com.cebolao.lotofacil.ui.components.game.GameRulesCard
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.SectionHeader
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.stats.FormattedText
import com.cebolao.lotofacil.ui.components.stats.StatisticsExplanationCard
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.components.settings.ThemeSettingsCard

// External URL constants
private const val URL_CAIXA = "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx"
// These URLs are intentionally empty - placeholders for future Terms and Privacy Policy links
private const val URL_TERMS = ""
private const val URL_PRIVACY = ""

@Composable
fun AboutScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    AboutScreenContent(
        themeMode = uiState.themeMode,
        accentPalette = uiState.accentPalette,
        onEvent = viewModel::onEvent,
        listState = listState
    )
}

@Composable
fun AboutScreenContent(
    themeMode: ThemeMode,
    accentPalette: AccentPalette,
    onEvent: (MainUiEvent) -> Unit,
    listState: LazyListState
) {
    val context = LocalContext.current
    var urlToOpen by remember { mutableStateOf<String?>(null) }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    AppScreen(
        title = stringResource(R.string.about_title),
        subtitle = stringResource(R.string.about_subtitle)
    ) { padding ->
        StandardPageLayout(
            scaffoldPadding = padding,
            listState = listState
        ) {
            item(key = "hero") {
                StudioHero()
            }

            item(key = "theme_settings") {
                SectionHeader(stringResource(R.string.about_appearance))
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
                    CaixaCard { urlToOpen = URL_CAIXA }
                }
            }

            item(key = "learn_more") {
                SectionHeader(stringResource(R.string.about_learn_more_header))
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
                            title = stringResource(R.string.about_source_title),
                            description = stringResource(R.string.about_source_desc)
                        )

                        AppDivider()

                        // Disclaimer
                        StandardInfoRow(
                            icon = AppIcons.Error,
                            title = stringResource(R.string.about_important_title),
                            description = stringResource(R.string.about_important_desc)
                        )

                        AppDivider()

                        // Privacidade
                        StandardInfoRow(
                            icon = AppIcons.InfoOutlined,
                            title = stringResource(R.string.about_privacy_title),
                            description = stringResource(R.string.about_privacy_desc)
                        )

                        AppDivider()

                        // Versão
                        StandardInfoRow(
                            icon = AppIcons.Info,
                            title = stringResource(R.string.about_version_title),
                            description = com.cebolao.lotofacil.BuildConfig.VERSION_NAME
                        )

                        // Links (se existirem)
                        if (URL_TERMS.isNotBlank() || URL_PRIVACY.isNotBlank()) {
                            AppDivider()

                            if (URL_TERMS.isNotBlank()) {
                                AboutItemRow(
                                    icon = AppIcons.Info,
                                    text = stringResource(R.string.about_terms)
                                ) { urlToOpen = URL_TERMS }
                            }

                            if (URL_PRIVACY.isNotBlank()) {
                                AboutItemRow(
                                    icon = AppIcons.Info,
                                    text = stringResource(R.string.about_privacy_policy)
                                ) { urlToOpen = URL_PRIVACY }
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

    // External Link Confirmation Dialog
    urlToOpen?.let { url ->
        AppConfirmationDialog(
            title = R.string.about_external_link_title,
            message = R.string.about_external_link_message,
            confirmText = R.string.about_external_link_confirm,
            onConfirm = {
                openUrl(url)
                urlToOpen = null
            },
            onDismiss = { urlToOpen = null },
            icon = AppIcons.Launch
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
            .semantics { if (isClickable) role = Role.Button }
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
