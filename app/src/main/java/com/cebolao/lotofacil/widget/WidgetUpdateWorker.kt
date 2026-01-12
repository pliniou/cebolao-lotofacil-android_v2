package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import com.cebolao.lotofacil.util.Formatters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.math.floor
import kotlin.math.max

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val gameRepository: GameRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
    }

    override suspend fun doWork(): Result = runCatching {
        updateAllWidgets()
        // Sync não pode “derrubar” o widget
        historyRepository.syncHistory().join()
        val synced = historyRepository.syncStatus.value is SyncStatus.Success
        if (synced) {
            updateAllWidgets()
        }
        Result.success()
    }.getOrElse { e ->
        Log.e(TAG, "Widget update failed", e)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    private suspend fun updateAllWidgets() {
        updateLastDrawWidgets()
        updateNextContestWidgets()
        updatePinnedGameWidgets()
    }

    private fun buildFinalWidgetViews(small: RemoteViews, medium: RemoteViews, large: RemoteViews, id: Int): RemoteViews {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WidgetUtils.buildResponsiveRemoteViews(small, medium, large)
        } else {
            val variant = WidgetUtils.getSizeVariant(context, id)
            when (variant) {
                WidgetSizeVariant.SMALL -> small
                WidgetSizeVariant.MEDIUM -> medium
                WidgetSizeVariant.LARGE -> large
            }
        }
    }

    private suspend fun updateLastDrawWidgets() {
        val ids = getWidgetIds(LastDrawWidgetProvider::class.java)
        if (ids.isEmpty()) return

        val lastDraw = historyRepository.getLastDraw()
        val appWidgetManager = AppWidgetManager.getInstance(context)

        ids.forEach { id ->
            val provider = LastDrawWidgetProvider::class.java

            // Monta as 3 variantes sempre (Android 12+ seleciona nativamente; pré-12 usa fallback)
            val small = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.SMALL), provider, id)
            val medium = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.MEDIUM), provider, id)
            val large = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.LARGE), provider, id)

            if (lastDraw != null) {
                applyLastDrawContent(small, WidgetSizeVariant.SMALL, id, lastDraw.contestNumber, lastDraw.numbers)
                applyLastDrawContent(medium, WidgetSizeVariant.MEDIUM, id, lastDraw.contestNumber, lastDraw.numbers)
                applyLastDrawContent(large, WidgetSizeVariant.LARGE, id, lastDraw.contestNumber, lastDraw.numbers)
            } else {
                showError(small, R.id.widget_numbers_container)
                showError(medium, R.id.widget_numbers_container)
                showError(large, R.id.widget_numbers_container)
            }

            val finalViews = buildFinalWidgetViews(small, medium, large, id)
            appWidgetManager.updateAppWidget(id, finalViews)
        }
    }

    private suspend fun updateNextContestWidgets() {
        val ids = getWidgetIds(NextContestWidgetProvider::class.java)
        if (ids.isEmpty()) return

        val details = historyRepository.getLastDrawDetails()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = NextContestWidgetProvider::class.java

        ids.forEach { id ->
            val small = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.SMALL), provider, id)
            val medium = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.MEDIUM), provider, id)
            val large = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.LARGE), provider, id)

            if (details != null && details.nextContestNumber != null) {
                val title = "${context.getString(R.string.widget_next_contest_title_generic)} ${details.nextContestNumber}"
                val date = details.nextContestDate ?: DEFAULT_PLACEHOLDER
                val prize = Formatters.formatCurrency(details.nextEstimatedPrize)
                
                applyNextContestContent(small, title, date, prize)
                applyNextContestContent(medium, title, date, prize)
                applyNextContestContent(large, title, date, prize)
            } else {
                showError(small, R.id.widget_content)
                showError(medium, R.id.widget_content)
                showError(large, R.id.widget_content)
            }

            val finalViews = buildFinalWidgetViews(small, medium, large, id)
            appWidgetManager.updateAppWidget(id, finalViews)
        }
    }

    private suspend fun updatePinnedGameWidgets() {
        val ids = getWidgetIds(PinnedGameWidgetProvider::class.java)
        if (ids.isEmpty()) return

        val pinned = gameRepository.pinnedGames.first().firstOrNull()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = PinnedGameWidgetProvider::class.java

        ids.forEach { id ->
            val small = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.SMALL), provider, id)
            val medium = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.MEDIUM), provider, id)
            val large = createRemoteViews(WidgetUtils.getLayoutIdFor(provider, WidgetSizeVariant.LARGE), provider, id)

            // Título é sempre o mesmo
            small.setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
            medium.setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
            large.setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))

            if (pinned != null) {
                applyPinnedContent(small, WidgetSizeVariant.SMALL, id, pinned.numbers)
                applyPinnedContent(medium, WidgetSizeVariant.MEDIUM, id, pinned.numbers)
                applyPinnedContent(large, WidgetSizeVariant.LARGE, id, pinned.numbers)
            } else {
                showError(small, R.id.widget_numbers_container, context.getString(R.string.widget_no_pinned_games))
                showError(medium, R.id.widget_numbers_container, context.getString(R.string.widget_no_pinned_games))
                showError(large, R.id.widget_numbers_container, context.getString(R.string.widget_no_pinned_games))
            }

            val finalViews = buildFinalWidgetViews(small, medium, large, id)
            appWidgetManager.updateAppWidget(id, finalViews)
        }
    }

    private fun applyLastDrawContent(
        v: RemoteViews,
        variant: WidgetSizeVariant,
        appWidgetId: Int,
        contestNumber: Int,
        numbers: Set<Int>
    ) {
        v.setTextViewText(
            R.id.widget_title,
            "${context.getString(R.string.widget_last_draw_title)}: $contestNumber"
        )

        populateGrid(
            views = v,
            containerId = R.id.widget_numbers_container,
            numbers = numbers,
            numbersPerRow = columnsForVariantOrRuntime(appWidgetId, variant),
            ballLayoutId = WidgetUtils.getBallLayoutIdFor(variant)
        )

        showContent(v, R.id.widget_numbers_container)
    }

    private fun applyPinnedContent(
        v: RemoteViews,
        variant: WidgetSizeVariant,
        appWidgetId: Int,
        numbers: Set<Int>
    ) {
        populateGrid(
            views = v,
            containerId = R.id.widget_numbers_container,
            numbers = numbers,
            numbersPerRow = columnsForVariantOrRuntime(appWidgetId, variant),
            ballLayoutId = WidgetUtils.getBallLayoutIdFor(variant)
        )

        showContent(v, R.id.widget_numbers_container)
    }

    private fun applyNextContestContent(v: RemoteViews, title: String, date: String, prize: String) {
        v.setTextViewText(R.id.widget_title, title)
        v.setTextViewText(R.id.widget_date, date)
        v.setTextViewText(R.id.widget_prize, prize)
        showContent(v, R.id.widget_content)
    }

    /**
     * Recursividade/Responsividade:
     * - Para API 31+, o launcher escolhe o RemoteViews pelo tamanho real;
     *   aqui usamos colunas por variante (consistentes).
     * - Para < 31, usamos o tamanho real (options) para ajustar colunas.
     */
    private fun columnsForVariantOrRuntime(appWidgetId: Int, variant: WidgetSizeVariant): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Perfil por variante (bom equilíbrio para 15 números)
            return when (variant) {
                WidgetSizeVariant.SMALL -> 4
                WidgetSizeVariant.MEDIUM -> 5
                WidgetSizeVariant.LARGE -> 5
            }
        }

        // < Android 12: calcula de acordo com minWidth/minHeight reais
        val opts = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val w = max(0, opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0))
        val h = max(0, opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0))

        val paddingDp = when (variant) {
            WidgetSizeVariant.SMALL -> 12
            WidgetSizeVariant.MEDIUM -> 16
            WidgetSizeVariant.LARGE -> 20
        }

        val ballDp = when (variant) {
            WidgetSizeVariant.SMALL -> 30
            WidgetSizeVariant.MEDIUM -> 36
            WidgetSizeVariant.LARGE -> 42
        }

        val marginDp = when (variant) {
            WidgetSizeVariant.SMALL -> 1
            WidgetSizeVariant.MEDIUM -> 2
            WidgetSizeVariant.LARGE -> 3
        }

        val effectiveW = (w - (paddingDp * 2)).coerceAtLeast(120)
        val cellDp = ballDp + (marginDp * 2)

        var cols = floor(effectiveW.toDouble() / cellDp.toDouble()).toInt().coerceIn(4, 6)

        // Altura curta: aumenta colunas para reduzir linhas
        if (h in 1..110) cols = (cols + 1).coerceAtMost(6)

        return cols
    }

    private fun getWidgetIds(cls: Class<out AppWidgetProvider>): IntArray {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, cls))
    }

    private fun createRemoteViews(
        layoutId: Int,
        cls: Class<out AppWidgetProvider>,
        id: Int
    ): RemoteViews {
        return RemoteViews(context.packageName, layoutId).apply {
            setOnClickPendingIntent(
                R.id.widget_refresh_button,
                WidgetUtils.getRefreshPendingIntent(context, cls, id)
            )
            setOnClickPendingIntent(R.id.widget_root, WidgetUtils.getOpenAppPendingIntent(context))
        }
    }

    private fun showContent(v: RemoteViews, contentId: Int) {
        v.setViewVisibility(R.id.widget_loading_text, View.GONE)
        v.setViewVisibility(contentId, View.VISIBLE)
    }

    private fun showError(
        v: RemoteViews,
        contentId: Int,
        msg: String = context.getString(R.string.widget_error_load)
    ) {
        v.setTextViewText(R.id.widget_loading_text, msg)
        v.setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
        v.setViewVisibility(contentId, View.GONE)
    }

    private fun populateGrid(
        views: RemoteViews,
        containerId: Int,
        numbers: Set<Int>,
        numbersPerRow: Int,
        ballLayoutId: Int
    ) {
        views.removeAllViews(containerId)

        numbers.sorted().chunked(numbersPerRow).forEach { row ->
            val rowView = RemoteViews(context.packageName, R.layout.widget_numbers_row)

            row.forEach { num ->
                val ball = RemoteViews(context.packageName, ballLayoutId).apply {
                    setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(num))
                }
                rowView.addView(R.id.widget_numbers_row_container, ball)
            }

            views.addView(containerId, rowView)
        }
    }
}
