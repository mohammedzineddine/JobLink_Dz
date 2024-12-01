package com.example.localjobs.pref

import com.google.firebase.auth.FirebaseAuth
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseAuth.getInstance() } // Provides FirebaseAuth instance
}
