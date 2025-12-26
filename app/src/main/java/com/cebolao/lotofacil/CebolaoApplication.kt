package com.cebolao.lotofacil

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class principal do app CebolaoLotofacil.
 * Configurado com Hilt para injeção de dependências e WorkManager.
 */
@HiltAndroidApp
class CebolaoApplication : Application(), Configuration.Provider {

    private companion object {
        const val WORK_NAME_WIDGET_UPDATE = "WidgetUpdateWork"
        const val WORK_NAME_HISTORY_STARTUP_SYNC = "HistoryStartupSync"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWidgetUpdate()
        enqueueStartupHistorySync()
    }

    private fun scheduleWidgetUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<com.cebolao.lotofacil.widget.WidgetUpdateWorker>(
            1,
            java.util.concurrent.TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME_WIDGET_UPDATE,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun enqueueStartupHistorySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<com.cebolao.lotofacil.widget.HistorySyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            WORK_NAME_HISTORY_STARTUP_SYNC,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
