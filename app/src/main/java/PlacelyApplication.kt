package com.example.placely

import android.app.Application
import com.example.placely.di.appModule
import com.example.placely.util.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlacelyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PlacelyApplication)
            modules(appModule)
        }

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}