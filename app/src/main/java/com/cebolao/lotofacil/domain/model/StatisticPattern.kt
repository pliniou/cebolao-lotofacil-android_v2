package com.cebolao.lotofacil.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Grid4x4
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector

enum class StatisticPattern(val title: String, val icon: ImageVector) {
    SUM("Soma das Dezenas", Icons.Outlined.Calculate),
    EVENS("Pares", Icons.Outlined.LooksOne),
    PRIMES("Primos", Icons.Outlined.Percent),
    FRAME("Moldura do Volante", Icons.Outlined.Grid4x4),
    FIBONACCI("Fibonacci", Icons.Outlined.Timeline),
    LINES("Linhas Cheias", Icons.Outlined.ViewHeadline),
    COLUMNS("Colunas Cheias", Icons.Outlined.ViewWeek),
    SEQUENCES("Sequências", Icons.Outlined.LinearScale),
    QUADRANTS("Quadrantes", Icons.Outlined.Dashboard)
}
