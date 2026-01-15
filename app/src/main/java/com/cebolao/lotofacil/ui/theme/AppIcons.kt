@file:Suppress("unused")

package com.cebolao.lotofacil.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Close
import androidx.compose.ui.graphics.vector.ImageVector
import com.cebolao.lotofacil.domain.model.FilterType

object AppIcons {
    val Shuffle = Icons.Filled.Shuffle
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
    val Warning = Icons.Filled.Warning
    val Info = Icons.Filled.Info
    val InfoOutlined = Icons.Outlined.Info
    val CloseOutlined = Icons.Outlined.Close
    val Save = Icons.Filled.Save
    val Feedback = Icons.Filled.Lightbulb

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
    val Wallet = Icons.Filled.AccountBalanceWallet
    val Paid = Icons.Filled.AttachMoney
    val TrendingUp = Icons.AutoMirrored.Filled.TrendingUp
    val TrendingDown = Icons.AutoMirrored.Filled.TrendingDown
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
        FilterType.MULTIPLES_OF_3 -> Icons.Default.TableRows
        FilterType.CENTER -> Icons.Default.ViewColumn
}
