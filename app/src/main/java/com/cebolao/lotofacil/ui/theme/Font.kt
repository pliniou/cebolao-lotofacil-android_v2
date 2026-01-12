package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R

val Gabarito = FontFamily(
    Font(R.font.gabarito_regular, FontWeight.Normal),
    Font(R.font.gabarito_medium, FontWeight.Medium),
    Font(R.font.gabarito_semibold, FontWeight.SemiBold),
    Font(R.font.gabarito_bold, FontWeight.Bold),
    Font(R.font.gabarito_extrabold, FontWeight.ExtraBold),
    Font(R.font.gabarito_black, FontWeight.Black)
)

val Outfit = FontFamily(
    Font(R.font.outfit_thin, FontWeight.Thin),
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
    Font(R.font.outfit_black, FontWeight.Black)
)

val StackSans = FontFamily(
    Font(R.font.stacksansnotch_light, FontWeight.Light),
    Font(R.font.stacksansnotch_regular, FontWeight.Normal),
    Font(R.font.stacksansnotch_medium, FontWeight.Medium),
    Font(R.font.stacksansnotch_semibold, FontWeight.SemiBold),
    Font(R.font.stacksansnotch_bold, FontWeight.Bold)
)

// Semantic aliases para facilitar manutenção de hierarquia tipográfica
val FontFamilyDisplay = Gabarito
val FontFamilyBody = Outfit
val FontFamilyNumeric = StackSans