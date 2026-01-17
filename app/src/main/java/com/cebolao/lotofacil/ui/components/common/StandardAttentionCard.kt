package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun StandardAttentionCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Warning
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            outlined = false,
            color = scheme.inverseSurface,
            contentPadding = Dimen.CardContentPadding
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = scheme.inversePrimary,
                        modifier = Modifier.size(Dimen.IconMedium)
                    )
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.inverseOnSurface
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.inverseOnSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun StandardAttentionCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            StandardAttentionCard(
                title = "Atenção",
                message = "Esta é uma mensagem de aviso importante que requer sua atenção."
            )
        }
    }
}
