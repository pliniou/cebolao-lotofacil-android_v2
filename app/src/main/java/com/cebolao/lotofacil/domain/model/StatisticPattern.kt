package com.cebolao.lotofacil.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Grid4x4
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector

enum class StatisticPattern(val filterType: FilterType, val icon: ImageVector) {
    SUM(FilterType.SOMA_DEZENAS, Icons.Outlined.Calculate),
    EVENS(FilterType.PARES, Icons.Outlined.LooksOne),
    PRIMES(FilterType.PRIMOS, Icons.Outlined.Percent),
    FRAME(FilterType.MOLDURA, Icons.Outlined.Grid4x4),
    FIBONACCI(FilterType.FIBONACCI, Icons.Outlined.Timeline),
    LINES(FilterType.LINHAS, Icons.Outlined.ViewHeadline),
    COLUMNS(FilterType.COLUNAS, Icons.Outlined.ViewWeek),
    SEQUENCES(FilterType.SEQUENCIAS, Icons.Outlined.LinearScale),
    QUADRANTS(FilterType.QUADRANTES, Icons.Outlined.Dashboard)
}
