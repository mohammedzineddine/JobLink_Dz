package com.example.localjobs.di

import com.example.localjobs.pref.PreferencesManager
import org.koin.compose.koinInject
import org.koin.dsl.module

val appModule = module {
    single { PreferencesManager(get()) }
}
