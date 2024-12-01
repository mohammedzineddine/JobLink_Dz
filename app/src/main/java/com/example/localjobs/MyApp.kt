package com.example.localjobs

import android.app.Application
import com.example.localjobs.di.appModule
import com.example.localjobs.di.firebaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(appModule, firebaseModule),
            )

        }
    }
}

