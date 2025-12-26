package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Layout padrão para páginas roláveis.
 * Integra com o padding do Scaffold e mantém margens horizontais consistentes.
 *
 * Correção importante:
 * - `content` NÃO deve ser @Composable, pois LazyColumn espera `LazyListScope.() -> Unit`.
 */
@Composable
fun StandardPageLayout(
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    addBottomSpace: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    val bottomPadding = scaffoldPadding.calculateBottomPadding() + if (addBottomSpace) {
        Dimen.BottomContentPadding
    } else {
        Dimen.SpacingMedium
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                top = scaffoldPadding.calculateTopPadding() + Dimen.SpacingShort,
                start = scaffoldPadding.calculateStartPadding(layoutDirection) + Dimen.ScreenPadding,
                end = scaffoldPadding.calculateEndPadding(layoutDirection) + Dimen.ScreenPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing),
            content = content
        )
    }
}