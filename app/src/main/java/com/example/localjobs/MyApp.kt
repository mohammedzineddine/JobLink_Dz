package com.example.localjobs.di

import android.app.Application
import com.example.localjobs.pref.firebaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)

            modules(listOf(appModule, firebaseModule))
        }
    }
}

