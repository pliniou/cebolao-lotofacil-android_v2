package br.com.loterias.cebolaolotofacil

import android.app.Application
import br.com.loterias.cebolaolotofacil.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Application entry point
 * Initializes Koin DI and Timber logging
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Koin dependency injection
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }

        Timber.i("App initialized - Version ${BuildConfig.VERSION_NAME}")
    }
}
