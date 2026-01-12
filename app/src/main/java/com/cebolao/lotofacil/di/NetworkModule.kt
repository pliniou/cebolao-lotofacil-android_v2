package com.cebolao.lotofacil.di

import com.cebolao.lotofacil.BuildConfig

import android.content.Context
import com.cebolao.lotofacil.data.network.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://servicebus2.caixa.gov.br/portaldeloterias/api/"
    private const val CACHE_DIR = "http_cache"
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024
    private const val TIMEOUT_CONNECT_SECONDS = 30L
    private const val TIMEOUT_READ_SECONDS = 30L
    private const val MEDIA_TYPE_JSON = "application/json"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        return Cache(File(context.cacheDir, CACHE_DIR), CACHE_SIZE_BYTES)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_READ_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(MEDIA_TYPE_JSON.toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
