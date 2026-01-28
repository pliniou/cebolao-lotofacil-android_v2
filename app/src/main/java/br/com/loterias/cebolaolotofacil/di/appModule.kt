package br.com.loterias.cebolaolotofacil.di

import br.com.loterias.cebolaolotofacil.data.LotofacilRepositoryImpl
import br.com.loterias.cebolaolotofacil.domain.repository.LotofacilRepository
import br.com.loterias.cebolaolotofacil.domain.usecase.GetRecentLotofacilResultsUseCase
import br.com.loterias.cebolaolotofacil.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    viewModel { HomeViewModel(get()) }

    // Repository
    single<LotofacilRepository> { LotofacilRepositoryImpl() }

    // UseCases
    single { GetRecentLotofacilResultsUseCase(get()) }
}
