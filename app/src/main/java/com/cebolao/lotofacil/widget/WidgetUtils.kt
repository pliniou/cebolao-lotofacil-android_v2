package com.cebolao.lotofacil.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cebolao.lotofacil.MainActivity
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.util.ACTION_REFRESH
import java.util.concurrent.TimeUnit
import kotlin.math.max

enum class WidgetSizeVariant { SMALL, MEDIUM, LARGE }

object WidgetUtils {

    internal const val UNIQUE_WIDGET_UPDATE_ON_DEMAND = "WidgetUpdateOnDemand"
    internal const val UNIQUE_WIDGET_UPDATE_PERIODIC = "WidgetUpdatePeriodic"

    /**
     * Breakpoints em dp: universais e estáveis entre densidades/launchers.
     * Observação: o Android 12+ vai usar o mapa SizeF->RemoteViews (responsivo nativo).
     * Este fallback é para < 12 (API < 31).
     */
    fun getSizeVariant(context: Context, appWidgetId: Int): WidgetSizeVariant {
        val opts = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val w = max(0, opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0))
        val h = max(0, opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0))

        return when {
            w in 1..199 -> WidgetSizeVariant.SMALL
            w >= 320 || h >= 200 -> WidgetSizeVariant.LARGE
            else -> WidgetSizeVariant.MEDIUM
        }
    }

    fun getLayoutIdFor(providerClass: Class<out AppWidgetProvider>, variant: WidgetSizeVariant): Int {
        return when (providerClass) {
            LastDrawWidgetProvider::class.java -> when (variant) {
                WidgetSizeVariant.SMALL -> R.layout.widget_last_draw_small
                WidgetSizeVariant.MEDIUM -> R.layout.widget_last_draw
                WidgetSizeVariant.LARGE -> R.layout.widget_last_draw_large
            }

            NextContestWidgetProvider::class.java -> when (variant) {
                WidgetSizeVariant.SMALL -> R.layout.widget_next_contest_small
                WidgetSizeVariant.MEDIUM -> R.layout.widget_next_contest
                WidgetSizeVariant.LARGE -> R.layout.widget_next_contest_large
            }

            PinnedGameWidgetProvider::class.java -> when (variant) {
                WidgetSizeVariant.SMALL -> R.layout.widget_pinned_game_small
                WidgetSizeVariant.MEDIUM -> R.layout.widget_pinned_game
                WidgetSizeVariant.LARGE -> R.layout.widget_pinned_game_large
            }

            else -> R.layout.widget_last_draw
        }
    }

    fun getBallLayoutIdFor(variant: WidgetSizeVariant): Int {
        return when (variant) {
            WidgetSizeVariant.SMALL -> R.layout.widget_number_ball_small
            WidgetSizeVariant.MEDIUM -> R.layout.widget_number_ball
            WidgetSizeVariant.LARGE -> R.layout.widget_number_ball_large
        }
    }

    @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.S)
    fun buildResponsiveRemoteViews(
        small: RemoteViews,
        medium: RemoteViews,
        large: RemoteViews
    ): RemoteViews {
        // Chaves em dp (SizeF widthDp, heightDp). Não precisam ser “pixel-perfect”.
        val sizeMap = mapOf(
            SizeF(180f, 110f) to small,
            SizeF(240f, 140f) to medium,
            SizeF(320f, 200f) to large
        )
        return RemoteViews(sizeMap)
    }

    fun enqueueOneTimeWidgetUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WIDGET_UPDATE_ON_DEMAND,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun schedulePeriodicWidgetUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WIDGET_UPDATE_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun getRefreshPendingIntent(
        context: Context,
        providerClass: Class<out AppWidgetProvider>,
        appWidgetId: Int
    ): PendingIntent {
        val intent = Intent(context, providerClass).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getOpenAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun applyLoadingState(
        views: RemoteViews,
        contentId: Int,
        loadingText: String
    ) {
        views.setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
        views.setViewVisibility(contentId, View.GONE)
        views.setTextViewText(R.id.widget_loading_text, loadingText)
    }

    fun showLoading(context: Context, providerClass: Class<out AppWidgetProvider>, appWidgetId: Int) {
        val contentId = when (providerClass) {
            LastDrawWidgetProvider::class.java -> R.id.widget_numbers_container
            NextContestWidgetProvider::class.java -> R.id.widget_content
            PinnedGameWidgetProvider::class.java -> R.id.widget_numbers_container
            else -> R.id.widget_numbers_container
        }

        val loading = context.getString(R.string.general_loading)

        val appWidgetManager = AppWidgetManager.getInstance(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val small = RemoteViews(context.packageName, getLayoutIdFor(providerClass, WidgetSizeVariant.SMALL)).apply {
                setOnClickPendingIntent(R.id.widget_refresh_button, getRefreshPendingIntent(context, providerClass, appWidgetId))
                setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))
                applyLoadingState(this, contentId, loading)
            }

            val medium = RemoteViews(context.packageName, getLayoutIdFor(providerClass, WidgetSizeVariant.MEDIUM)).apply {
                setOnClickPendingIntent(R.id.widget_refresh_button, getRefreshPendingIntent(context, providerClass, appWidgetId))
                setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))
                applyLoadingState(this, contentId, loading)
            }

            val large = RemoteViews(context.packageName, getLayoutIdFor(providerClass, WidgetSizeVariant.LARGE)).apply {
                setOnClickPendingIntent(R.id.widget_refresh_button, getRefreshPendingIntent(context, providerClass, appWidgetId))
                setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))
                applyLoadingState(this, contentId, loading)
            }

            val responsive = buildResponsiveRemoteViews(small, medium, large)
            appWidgetManager.updateAppWidget(appWidgetId, responsive)
            return
        }

        // Fallback < Android 12
        val variant = getSizeVariant(context, appWidgetId)
        val layoutId = getLayoutIdFor(providerClass, variant)
        val views = RemoteViews(context.packageName, layoutId).apply {
            setOnClickPendingIntent(R.id.widget_refresh_button, getRefreshPendingIntent(context, providerClass, appWidgetId))
            setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))
            applyLoadingState(this, contentId, loading)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
