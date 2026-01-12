package com.cebolao.lotofacil

import android.annotation.SuppressLint
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
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
        const val WORK_NAME_HISTORY_STARTUP_SYNC = "HistoryStartupSync"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            // Logs mais verbosos em debug para diagnosticar workers; INFO em release
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.VERBOSE else android.util.Log.INFO
            )
            .build()

    @SuppressLint("DefaultUncaughtExceptionDelegation")
    override fun onCreate() {
        super.onCreate()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CRASH_REPORT", "Uncaught exception in thread ${thread.name}", throwable)
            // Opcional: chamar o handler padrão antigo se necessário, mas para debug isso basta
            // exitProcess(1) // Pode ser necessário terminar o processo
        }

        scheduleWidgetUpdate()
        enqueueStartupHistorySync()
    }

    private fun scheduleWidgetUpdate() {
        com.cebolao.lotofacil.widget.WidgetUtils.schedulePeriodicWidgetUpdate(this)
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
