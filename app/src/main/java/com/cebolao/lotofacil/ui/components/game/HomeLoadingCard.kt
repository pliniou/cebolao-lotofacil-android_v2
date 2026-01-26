package com.cebolao.lotofacil.ui.components.game

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

data class HomeLoadingStatus(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val progress: Float
)

@Composable
fun HomeLoadingCard(
    status: HomeLoadingStatus,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val progress by animateFloatAsState(
        targetValue = status.progress,
        animationSpec = Motion.Tween.medium(),
        label = "home_loading_progress"
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
                Icon(
                    imageVector = AppIcons.Refresh,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(Dimen.IconSmall)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    AnimatedContent(
                        targetState = status.titleRes,
                        transitionSpec = {
                            fadeIn(animationSpec = Motion.Tween.medium()) togetherWith
                                fadeOut(animationSpec = Motion.Tween.fast())
                        },
                        label = "home_loading_title"
                    ) { titleRes ->
                        Text(
                            text = stringResource(titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                    }
                    AnimatedContent(
                        targetState = status.messageRes,
                        transitionSpec = {
                            fadeIn(animationSpec = Motion.Tween.medium()) togetherWith
                                fadeOut(animationSpec = Motion.Tween.fast())
                        },
                        label = "home_loading_message"
                    ) { messageRes ->
                        Text(
                            text = stringResource(messageRes),
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
