package com.example.localjobs.FirebaseAuthCheck

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Get the current logged-in user
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    // Log in with email and password
    suspend fun loginWithEmailPassword(email: String, password: String): Boolean {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("AuthError", "Login failed", e)
            false
        }
    }

    // Log out
    fun logout() {
        firebaseAuth.signOut()
    }

    // Register user
    suspend fun registerUser(email: String, password: String): Boolean {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("AuthError", "Registration failed", e)
            false
        }
    }
}
