package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun StudioHero(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = false,
        color = scheme.surfaceContainer,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Surface(
                color = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.large,
                tonalElevation = Dimen.Elevation.None,
                shadowElevation = Dimen.Elevation.None
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_lotofacil_logo),
                    contentDescription = stringResource(R.string.studio_name),
                    modifier = Modifier
                        .padding(Dimen.SpacingShort)
                        .size(Dimen.Logo)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
            ) {
                Text(
                    text = stringResource(id = R.string.studio_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(id = R.string.studio_slogan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
