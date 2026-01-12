package com.cebolao.lotofacil.di

import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.presentation.viewmodel.CheckerViewModelCoroutineDependencies
import com.cebolao.lotofacil.presentation.viewmodel.CheckerViewModelDependencies
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideCheckerViewModelDependencies(
        checkGameUseCase: CheckGameUseCase,
        saveGameUseCase: SaveGameUseCase,
        metricsCalculator: GameMetricsCalculator,
        historyRepository: HistoryRepository,
        checkRunRepository: CheckRunRepository,
        logger: Logger
    ): CheckerViewModelDependencies {
        return CheckerViewModelDependencies(
            checkGameUseCase = checkGameUseCase,
            saveGameUseCase = saveGameUseCase,
            metricsCalculator = metricsCalculator,
            historyRepository = historyRepository,
            checkRunRepository = checkRunRepository,
            logger = logger
        )
    }

    @Provides
    @ViewModelScoped
    fun provideCheckerViewModelCoroutineDependencies(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        @ApplicationScope externalScope: CoroutineScope
    ): CheckerViewModelCoroutineDependencies {
        return CheckerViewModelCoroutineDependencies(
            defaultDispatcher = defaultDispatcher,
            externalScope = externalScope
        )
    }
}
