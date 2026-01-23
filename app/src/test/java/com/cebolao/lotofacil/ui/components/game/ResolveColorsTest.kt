package com.cebolao.lotofacil.ui.components.game

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.cebolao.lotofacil.ui.theme.SuccessColor
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveColorsTest {
    @Test
    fun primaryVariant_uses_primaryContainer_and_onPrimaryContainer() {
        val scheme = lightColorScheme()
        val colors = resolveColors(false, NumberBallVariant.Primary, scheme, null)

        assertEquals(scheme.primaryContainer, colors.container)
        assertEquals(scheme.onPrimaryContainer, colors.content)
        assertEquals(Color.Transparent, colors.border)
    }

    @Test
    fun hitVariant_uses_SuccessColor_and_white_content() {
        val scheme = lightColorScheme()
        val colors = resolveColors(false, NumberBallVariant.Hit, scheme, null)

        assertEquals(SuccessColor, colors.container)
        assertEquals(Color.White, colors.content)
        assertEquals(Color.Transparent, colors.border)
    }

    @Test
    fun neutral_with_custom_background_chooses_high_contrast_content() {
        val dark = resolveColors(false, NumberBallVariant.Neutral, lightColorScheme(), Color.Black)
        assertEquals(Color.Black, dark.container)
        assertEquals(Color.White, dark.content)

        val light = resolveColors(false, NumberBallVariant.Neutral, lightColorScheme(), Color.White)
        assertEquals(Color.White, light.container)
        assertEquals(Color.Black, light.content)
    }
}
