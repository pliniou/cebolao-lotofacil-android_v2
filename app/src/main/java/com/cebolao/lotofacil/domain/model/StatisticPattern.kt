package com.cebolao.lotofacil.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Grid4x4
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

enum class StatisticPattern(val icon: ImageVector) {
    SUM(Icons.Outlined.Calculate),
    EVENS(Icons.Outlined.LooksOne),
    PRIMES(Icons.Outlined.Percent),
    FRAME(Icons.Outlined.Grid4x4),
    CENTER(Icons.Outlined.Dashboard),
    FIBONACCI(Icons.Outlined.Timeline),
    MULTIPLES_OF_3(Icons.Outlined.Grid4x4),
    SEQUENCES(Icons.Outlined.LinearScale)
}
