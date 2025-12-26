package com.cebolao.lotofacil.ui.components.common

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun AppDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = Dimen.Border.Hairline,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
    )
}
