package com.example.localjobs.di

import com.example.localjobs.Sessions.UserSessionManager
import com.example.localjobs.pref.PreferencesManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.compose.koinInject
import org.koin.dsl.module

val appModule = module {
    single { PreferencesManager(get()) }
    viewModel { JobListViewModel() }
    single { UserSessionManager(androidContext()) }
}
