package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(
            start = Dimen.ScreenPadding,
            end = Dimen.ScreenPadding,
            top = Dimen.SectionSpacing,
            bottom = Dimen.SmallPadding
        )
    )
}
