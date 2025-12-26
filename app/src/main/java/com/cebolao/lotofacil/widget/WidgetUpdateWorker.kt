package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import com.cebolao.lotofacil.util.Formatters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

private const val TAG = "WidgetUpdateWorker"
private const val NUMBERS_PER_ROW = 5

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val gameRepository: GameRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = runCatching {
        // Fase 2: não depender de sync no init do repository; atualiza histórico antes de renderizar widgets.
        historyRepository.syncHistory().join()
        updateLastDrawWidgets()
        updateNextContestWidgets()
        updatePinnedGameWidgets()
        Result.success()
    }.getOrElse { e ->
        Log.e(TAG, "Widget update failed", e)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    private suspend fun updateLastDrawWidgets() {
        val ids = getWidgetIds(LastDrawWidgetProvider::class.java)
        if (ids.isEmpty()) return
        val lastDraw = historyRepository.getLastDraw()

        ids.forEach { id ->
            val views = createRemoteViews(R.layout.widget_last_draw, LastDrawWidgetProvider::class.java, id)
            if (lastDraw != null) {
                views.setTextViewText(R.id.widget_title, "${context.getString(R.string.widget_last_draw_title)}: ${lastDraw.contestNumber}")
                populateGrid(views, R.id.widget_numbers_container, lastDraw.numbers)
                showContent(views, R.id.widget_numbers_container)
            } else showError(views, R.id.widget_numbers_container)
            updateAppWidget(id, views)
        }
    }

    private suspend fun updateNextContestWidgets() {
        val ids = getWidgetIds(NextContestWidgetProvider::class.java)
        if (ids.isEmpty()) return
        val details = historyRepository.getLastDrawDetails()

        ids.forEach { id ->
            val views = createRemoteViews(R.layout.widget_next_contest, NextContestWidgetProvider::class.java, id)
            if (details != null && details.nextContestNumber != null) {
                views.setTextViewText(R.id.widget_title, "${context.getString(R.string.widget_next_contest_title_generic)} ${details.nextContestNumber}")
                views.setTextViewText(R.id.widget_date, details.nextContestDate ?: DEFAULT_PLACEHOLDER)
                views.setTextViewText(R.id.widget_prize, Formatters.formatCurrency(details.nextEstimatedPrize))
                showContent(views, R.id.widget_content)
            } else showError(views, R.id.widget_content)
            updateAppWidget(id, views)
        }
    }

    private suspend fun updatePinnedGameWidgets() {
        val ids = getWidgetIds(PinnedGameWidgetProvider::class.java)
        if (ids.isEmpty()) return

        // Uso correto de first() pois pinnedGames é um StateFlow que sempre tem valor
        val pinned = gameRepository.pinnedGames.first().firstOrNull()

        ids.forEach { id ->
            val views = createRemoteViews(R.layout.widget_pinned_game, PinnedGameWidgetProvider::class.java, id)
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
            if (pinned != null) {
                populateGrid(views, R.id.widget_numbers_container, pinned.numbers)
                showContent(views, R.id.widget_numbers_container)
            } else showError(views, R.id.widget_numbers_container, context.getString(R.string.widget_no_pinned_games))
            updateAppWidget(id, views)
        }
    }

    private fun getWidgetIds(cls: Class<out AppWidgetProvider>) = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, cls))
    private fun createRemoteViews(layoutId: Int, cls: Class<out AppWidgetProvider>, id: Int) = RemoteViews(context.packageName, layoutId).apply {
        setOnClickPendingIntent(R.id.widget_refresh_button, WidgetUtils.getRefreshPendingIntent(context, cls, id))
    }
    private fun showContent(v: RemoteViews, id: Int) { v.setViewVisibility(R.id.widget_loading_text, View.GONE); v.setViewVisibility(id, View.VISIBLE) }
    private fun showError(v: RemoteViews, id: Int, msg: String = context.getString(R.string.widget_error_load)) {
        v.setTextViewText(R.id.widget_loading_text, msg)
        v.setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
        v.setViewVisibility(id, View.GONE)
    }
    private fun populateGrid(v: RemoteViews, containerId: Int, numbers: Set<Int>) {
        v.removeAllViews(containerId)
        numbers.sorted().chunked(NUMBERS_PER_ROW).forEach { row ->
            val rowView = RemoteViews(context.packageName, R.layout.widget_numbers_row)
            row.forEach { num ->
                val ball = RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                    setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(num))
                }
                rowView.addView(R.id.widget_numbers_row_container, ball)
            }
            v.addView(containerId, rowView)
        }
    }
    private fun updateAppWidget(id: Int, views: RemoteViews) = AppWidgetManager.getInstance(context).updateAppWidget(id, views)
}
