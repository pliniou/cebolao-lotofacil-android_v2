package com.cebolao.lotofacil.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.util.ACTION_REFRESH

object WidgetUtils {

    fun enqueueOneTimeWidgetUpdate(context: Context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build())
    }

    fun getRefreshPendingIntent(context: Context, providerClass: Class<out AppWidgetProvider>, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, providerClass).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun showLoading(context: Context, providerClass: Class<out AppWidgetProvider>, appWidgetId: Int) {
        val (layoutId, contentId) = when (providerClass) {
            LastDrawWidgetProvider::class.java -> R.layout.widget_last_draw to R.id.widget_numbers_container
            NextContestWidgetProvider::class.java -> R.layout.widget_next_contest to R.id.widget_content
            PinnedGameWidgetProvider::class.java -> R.layout.widget_pinned_game to R.id.widget_numbers_container
            else -> return
        }

        val views = RemoteViews(context.packageName, layoutId).apply {
            setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
            setViewVisibility(contentId, View.GONE)
            setTextViewText(R.id.widget_loading_text, context.getString(R.string.general_loading))
            setOnClickPendingIntent(R.id.widget_refresh_button, getRefreshPendingIntent(context, providerClass, appWidgetId))
        }
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
    }
}
