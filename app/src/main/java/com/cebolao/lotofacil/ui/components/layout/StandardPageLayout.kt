package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Layout padrão para páginas roláveis.
 * Integra com o padding do Scaffold e mantém margens horizontais consistentes.
 */
@Composable
fun StandardPageLayout(
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    addBottomSpace: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    // Cache bottom padding calculation to avoid recomputing on every recomposition
    val bottomPadding = remember(scaffoldPadding, addBottomSpace) {
        scaffoldPadding.calculateBottomPadding() + if (addBottomSpace) {
            Dimen.BottomContentPadding
        } else {
            Dimen.SpacingMedium
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = Dimen.LayoutMaxWidth)
                .fillMaxWidth()
                .imePadding(),
            state = listState,
            contentPadding = remember(scaffoldPadding, layoutDirection, bottomPadding) {
                PaddingValues(
                    top = scaffoldPadding.calculateTopPadding() + Dimen.Spacing12,
                    start = scaffoldPadding.calculateStartPadding(layoutDirection) + Dimen.ScreenPadding,
                    end = scaffoldPadding.calculateEndPadding(layoutDirection) + Dimen.ScreenPadding,
                    bottom = bottomPadding
                )
            },
            verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
