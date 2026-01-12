@file:Suppress("SameParameterValue")
package com.cebolao.lotofacil.ui.components.stats

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun FormattedText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val annotatedString = remember(text) { htmlToAnnotatedString(text) }

    Text(
        text = annotatedString,
        style = style,
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun TitleWithIcon(
    text: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
    ) {
        when {
            iconVector != null -> {
                // Container tonal flat (Material 3) para ícone vetorial
                Surface(
                    color = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Box(
                        modifier = Modifier.padding(Dimen.ExtraSmallPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(Dimen.MediumIcon)
                        )
                    }
                }
            }

            iconRes != null -> {
                // Para drawable (possível raster), mantém renderização “como é”
                Surface(
                    color = scheme.surfaceVariant,
                    contentColor = scheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Box(
                        modifier = Modifier.padding(Dimen.ExtraSmallPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(Dimen.MediumIcon)
                        )
                    }
                }
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InfoPoint(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

private fun htmlToAnnotatedString(html: String): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)

    return buildAnnotatedString {
        // Base text
        append(spanned.toString())

        // Span mapping (mantém compatibilidade e estética consistente com o tema)
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            when (span) {
                is android.text.style.StyleSpan -> when (span.style) {
                    android.graphics.Typeface.BOLD ->
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)

                    android.graphics.Typeface.ITALIC ->
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                }

                is android.text.style.UnderlineSpan ->
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)

                is android.text.style.StrikethroughSpan ->
                    addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TitleWithIconPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TitleWithIcon(
                text = "Example Title",
                iconVector = AppIcons.Info
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoPointPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoPoint(
                title = "Point Title",
                description = "This is a description of the info point."
            )
        }
    }
}
