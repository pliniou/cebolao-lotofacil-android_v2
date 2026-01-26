package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * App typography definitions.
 *
 * Font Usage Guidelines:
 * - **Display**: Use for large, impactful headers (e.g. Onboarding, Hero sections). Font: Gabarito.
 * - **Headline**: Use for main screen titles and section headers. Font: Outfit.
 * - **Title**: Use for card titles and sub-sections. Font: Outfit (Medium/SemiBold).
 * - **Body**: Use for long-form text, descriptions, and messages. Font: Outfit (Regular).
 * - **Label**: Use for buttons, tags, and small utility text. Font: Outfit (Medium).
 * - **Numeric**: Use EXPRESSLY for lottery numbers and statistics data. Font: StackSans.
 */
private fun buildAdaptiveTypography(scale: Float): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamilyDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = scaleSp(32.sp, scale),
            lineHeight = scaleSp(40.sp, scale),
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamilyDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = scaleSp(28.sp, scale),
            lineHeight = scaleSp(36.sp, scale),
            letterSpacing = (-0.25).sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamilyDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = scaleSp(24.sp, scale),
            lineHeight = scaleSp(32.sp, scale)
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.Bold,
            fontSize = scaleSp(28.sp, scale),
            lineHeight = scaleSp(36.sp, scale),
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.Bold,
            fontSize = scaleSp(24.sp, scale),
            lineHeight = scaleSp(32.sp, scale)
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaleSp(20.sp, scale),
            lineHeight = scaleSp(28.sp, scale)
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaleSp(18.sp, scale),
            lineHeight = scaleSp(24.sp, scale),
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaleSp(16.sp, scale),
            lineHeight = scaleSp(24.sp, scale),
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.Medium,
            fontSize = scaleSp(14.sp, scale),
            lineHeight = scaleSp(20.sp, scale),
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.Normal,
            fontSize = scaleSp(16.sp, scale),
            lineHeight = scaleSp(24.sp, scale),
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.Normal,
            fontSize = scaleSp(14.sp, scale),
            lineHeight = scaleSp(20.sp, scale),
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamilyBody,
            fontWeight = FontWeight.Normal,
            fontSize = scaleSp(12.sp, scale),
            lineHeight = scaleSp(16.sp, scale),
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamilyNumeric,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaleSp(14.sp, scale),
            lineHeight = scaleSp(20.sp, scale),
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamilyNumeric,
            fontWeight = FontWeight.Medium,
            fontSize = scaleSp(12.sp, scale),
            lineHeight = scaleSp(16.sp, scale),
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamilyNumeric,
            fontWeight = FontWeight.Medium,
            fontSize = scaleSp(11.sp, scale),
            lineHeight = scaleSp(16.sp, scale),
            letterSpacing = 0.5.sp
        )
    )
}

@Composable
fun rememberAdaptiveTypography(): Typography {
    val scale = rememberTypographyScale()
    return remember(scale) { buildAdaptiveTypography(scale) }
}
