@file:Suppress("unused")

package com.cebolao.lotofacil.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.ui.graphics.vector.ImageVector
import com.cebolao.lotofacil.domain.model.FilterType

object AppIcons {
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val Lightbulb = Icons.Filled.Lightbulb
    val ArrowForward = Icons.AutoMirrored.Filled.ArrowForward
    val Home = Icons.Filled.Home
    val HomeOutlined = Icons.Outlined.Home
    val Launch = Icons.AutoMirrored.Filled.Launch
    val Share = Icons.Filled.Share
    val Settings = Icons.Filled.Settings
    val Delete = Icons.Filled.Delete
    val DeleteSweep = Icons.Filled.DeleteSweep
    val Add = Icons.Filled.Add
    val Remove = Icons.Filled.Remove
    val Cancel = Icons.Filled.Cancel
    val Send = Icons.AutoMirrored.Filled.Send
    val PinFilled = Icons.Filled.PushPin
    val PinOutlined = Icons.Outlined.PushPin

    // Feedback
    val Success = Icons.Filled.CheckCircle
    val Check = Icons.Filled.Check
    val Error = Icons.Filled.Error
    val Info = Icons.Filled.Info
    val InfoOutlined = Icons.Outlined.Info
    val Save = Icons.Filled.Save

    // Domain Specific
    val Table = Icons.Filled.TableChart
    val List = Icons.AutoMirrored.Filled.ListAlt
    val ListOutlined = Icons.AutoMirrored.Outlined.ListAlt
    val Tune = Icons.Filled.Tune
    val TuneOutlined = Icons.Outlined.Tune
    val Analytics = Icons.Filled.Analytics
    val AnalyticsOutlined = Icons.Outlined.Analytics
    val StarFilled = Icons.Filled.Star
    val StarOutlined = Icons.Outlined.StarBorder
}

val FilterType.filterIcon: ImageVector
    get() = when (this) {
        FilterType.SOMA_DEZENAS -> Icons.Default.Calculate
        FilterType.PARES -> Icons.Default.Numbers
        FilterType.PRIMOS -> Icons.Default.Percent
        FilterType.MOLDURA -> Icons.Default.Grid4x4
        FilterType.FIBONACCI -> Icons.Default.Timeline
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> Icons.Default.Repeat
        FilterType.SEQUENCIAS -> Icons.Default.Link
        FilterType.MULTIPLES_OF_3 -> Icons.Default.TableRows // Reusing TableRows or similar
        FilterType.CENTER -> Icons.Default.ViewColumn // Reusing ViewColumn or similar
    }
