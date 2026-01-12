package com.cebolao.lotofacil.domain.usecase

import android.content.Context
import com.cebolao.lotofacil.domain.service.GenerationTelemetry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case para exportar métricas de telemetria para arquivo
 */
class ExportTelemetryUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    /**
     * Exports telemetry metrics to a CSV file
     * @return File path if successful, null otherwise
     */
    suspend operator fun invoke(telemetry: GenerationTelemetry): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "telemetry_$timestamp.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val csv = buildString {
                appendLine("Metric,Value")
                appendLine("Seed,${telemetry.seed}")
                appendLine("Strategy,${telemetry.strategy}")
                appendLine("Duration (ms),${telemetry.durationMs}")
                appendLine("Total Attempts,${telemetry.totalAttempts}")
                appendLine("Successful Games,${telemetry.successfulGames}")
                appendLine("Success Rate,${"%.2f".format(telemetry.successRate * 100)}%")
                appendLine("Rejection Rate,${"%.2f".format(telemetry.rejectionRate * 100)}%")
                appendLine("Avg Time Per Game (ms),${telemetry.avgTimePerGame}")
                appendLine("Total Rejections,${telemetry.totalRejections}")
                appendLine("Most Restrictive Filter,${telemetry.mostRestrictiveFilter ?: "N/A"}")
                appendLine()
                appendLine("Filter,Rejections")
                telemetry.rejectionsByFilter.forEach { (filter, count) ->
                    appendLine("$filter,$count")
                }
            }
            
            file.writeText(csv)
            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }
}
