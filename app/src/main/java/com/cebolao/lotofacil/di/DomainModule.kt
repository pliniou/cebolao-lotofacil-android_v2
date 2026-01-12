package com.cebolao.lotofacil.di

import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideGameMetricsCalculator(): GameMetricsCalculator {
        return GameMetricsCalculator()
    }
}
