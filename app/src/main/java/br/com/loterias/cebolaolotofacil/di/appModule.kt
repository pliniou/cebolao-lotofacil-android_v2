package br.com.loterias.cebolaolotofacil.di

import androidx.room.Room
import br.com.loterias.cebolaolotofacil.BuildConfig
import br.com.loterias.cebolaolotofacil.data.LotofacilRepositoryImpl
import br.com.loterias.cebolaolotofacil.data.local.db.LotofacilDatabase
import br.com.loterias.cebolaolotofacil.data.remote.LotofacilApiService
import br.com.loterias.cebolaolotofacil.data.remote.interceptor.HeaderInterceptor
import br.com.loterias.cebolaolotofacil.data.remote.interceptor.ResponseLoggingInterceptor
import br.com.loterias.cebolaolotofacil.data.remote.interceptor.RetryOnHttp429Interceptor
import br.com.loterias.cebolaolotofacil.domain.repository.LotofacilRepository
import br.com.loterias.cebolaolotofacil.domain.usecase.GetRecentLotofacilResultsUseCase
import br.com.loterias.cebolaolotofacil.presentation.viewmodel.HomeViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Koin dependency injection module
 * Configures all dependencies including network, database, and ViewModels
 */
val appModule = module {

    // Moshi for JSON serialization
    single {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // OkHttpClient with logging and timeouts
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(ResponseLoggingInterceptor())
            .addInterceptor(HttpLoggingInterceptor { message ->
                Timber.d(message)
            }.apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            })
            .addNetworkInterceptor(RetryOnHttp429Interceptor(maxRetries = 3))
            .build()
    }

    // Retrofit API client
    single {
        Retrofit.Builder()
            .baseUrl(LotofacilApiService.BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    // API Service
    single {
        get<Retrofit>().create(LotofacilApiService::class.java)
    }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            LotofacilDatabase::class.java,
            LotofacilDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    // DAO
    single {
        get<LotofacilDatabase>().lotofacilResultDao()
    }

    // Repository
    single<LotofacilRepository> {
        LotofacilRepositoryImpl(
            apiService = get(),
            localDao = get()
        )
    }

    // UseCases
    single { GetRecentLotofacilResultsUseCase(get()) }

    // ViewModels
    viewModel { HomeViewModel(get()) }
}
