package com.cebolao.lotofacil.domain.model

/**
 * Represents the current state of database loading operations.
 */
sealed class DatabaseLoadingState {
    data object Idle : DatabaseLoadingState()
    data class Loading(
        val phase: LoadingPhase,
        val progress: Float,
        val loadedCount: Int,
        val totalCount: Int
    ) : DatabaseLoadingState()
    data class Completed(val loadedCount: Int) : DatabaseLoadingState()
    data class Failed(val error: String, val exception: Throwable?) : DatabaseLoadingState()
}

/**
 * Phases of the loading process.
 */
enum class LoadingPhase {
    CHECKING,
    READING_ASSETS,
    PARSING_DATA,
    SAVING_TO_DATABASE,
    FINALIZING
}
