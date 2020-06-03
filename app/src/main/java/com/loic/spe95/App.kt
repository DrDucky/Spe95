package com.loic.spe95

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.loic.spe95.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(viewModelModule))
        }

        // Initialize the Google Places SDK
        Places.initialize(applicationContext, BuildConfig.API_PLACES_KEY)
    }
}