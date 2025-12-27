package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.cebolao.lotofacil.util.ACTION_REFRESH

abstract class BaseLotofacilWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            WidgetUtils.showLoading(context, this::class.java, id)
        }
        WidgetUtils.enqueueOneTimeWidgetUpdate(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        // Re-render ao redimensionar (small/medium/large)
        WidgetUtils.showLoading(context, this::class.java, appWidgetId)
        WidgetUtils.enqueueOneTimeWidgetUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            val id = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WidgetUtils.showLoading(context, this::class.java, id)
                WidgetUtils.enqueueOneTimeWidgetUpdate(context)
            }
        }
    }
}
