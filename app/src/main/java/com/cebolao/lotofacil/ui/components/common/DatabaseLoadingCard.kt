package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.DatabaseLoadingState
import com.cebolao.lotofacil.domain.model.LoadingPhase
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun DatabaseLoadingCard(
    loadingState: DatabaseLoadingState,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme

    when (loadingState) {
        is DatabaseLoadingState.Idle -> {
            Spacer(modifier = modifier)
        }
        is DatabaseLoadingState.Loading -> {
            val phaseTitle = when (loadingState.phase) {
                LoadingPhase.CHECKING -> R.string.db_loading_checking
                LoadingPhase.READING_ASSETS -> R.string.db_loading_reading_assets
                LoadingPhase.PARSING_DATA -> R.string.db_loading_parsing
                LoadingPhase.SAVING_TO_DATABASE -> R.string.db_loading_saving
                LoadingPhase.FINALIZING -> R.string.db_loading_finalizing
            }

            val progress by animateFloatAsState(
                targetValue = loadingState.progress,
                animationSpec = Motion.Tween.medium(),
                label = "db_loading_progress"
            )

            AppCard(
                modifier = modifier,
                outlined = true,
                contentPadding = Dimen.Spacing16
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.IconSmall),
                            strokeWidth = Dimen.Border.Thin,
                            color = scheme.primary
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                        ) {
                            Text(
                                text = stringResource(phaseTitle),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            if (loadingState.totalCount > 0) {
                                Text(
                                    text = pluralStringResource(
                                        id = R.plurals.db_loading_progress,
                                        count = loadingState.totalCount,
                                        loadingState.loadedCount,
                                        loadingState.totalCount
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        color = scheme.primary,
                        trackColor = scheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        is DatabaseLoadingState.Completed -> {
            AppCard(
                modifier = modifier,
                outlined = true,
                contentPadding = Dimen.Spacing16
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(Dimen.IconSmall)
                    )
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.db_loading_complete,
                            count = loadingState.loadedCount,
                            loadingState.loadedCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface
                    )
                }
            }
        }
        is DatabaseLoadingState.Failed -> {
            AppCard(
                modifier = modifier,
                outlined = true,
                contentPadding = Dimen.Spacing16
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                    ) {
                        Icon(
                            imageVector = AppIcons.Error,
                            contentDescription = null,
                            tint = scheme.error,
                            modifier = Modifier.size(Dimen.IconSmall)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                        ) {
                            Text(
                                text = stringResource(R.string.general_error_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = loadingState.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
            if (onRetry != null) {
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(R.string.db_loading_retry))
                }
            }
        }
    }
}
    }
}
