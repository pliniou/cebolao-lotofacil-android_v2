package br.com.loterias.cebolaolotofacil.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun CebolaoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography(),
        content = content
    )
}
