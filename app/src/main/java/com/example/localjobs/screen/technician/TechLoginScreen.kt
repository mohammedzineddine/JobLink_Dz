package com.example.localjobs.screen.tech

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.R
import com.example.localjobs.screen.technician.HomeTch
import com.example.localjobs.screen.technician.TechRegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TechLoginScreen : Screen {

    @Composable
    override fun Content() {
        val auth = FirebaseAuth.getInstance()
        val navigator = LocalNavigator.currentOrThrow
        val database = Firebase.database.reference

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var isPasswordVisible by remember { mutableStateOf(false) }

        // Handle back press behavior
        BackHandler {
            if (isLoading) {
                errorMessage = "Login in progress, please wait."
            } else {
                navigator.pop()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.mobile_login), // You can change this to a tech-specific image
                contentDescription = "App Logo",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tech Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "E-mail",
                        modifier = Modifier.size(24.dp)
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                            if (email.isNotEmpty() && password.isNotEmpty() && !isLoading) {
                                isLoading = true
                            }
                            true
                        } else {
                            false
                        }
                    },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Password",
                        modifier = Modifier.size(24.dp)
                    )
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isPasswordVisible = !isPasswordVisible }
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    // Fetch tech data from Realtime Database
                                    val techId = auth.currentUser?.uid
                                    if (techId != null) {
                                        database.child("Technicians").child(techId).get()
                                            .addOnSuccessListener { dataSnapshot ->
                                                val techData = dataSnapshot.value as? Map<*, *>
                                                if (techData != null) {
                                                    // Navigate to tech home screen with tech data
                                                    navigator.replace(HomeTch())
                                                } else {
                                                    errorMessage = "Tech data not found."
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Failed to fetch tech data: ${e.message}"
                                            }
                                    } else {
                                        errorMessage = "Tech ID is null."
                                    }
                                } else {
                                    errorMessage = "Authentication failed: ${task.exception?.message}"
                                }
                            }
                    } else {
                        errorMessage = "Please fill in both fields."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Forgot Password?",
                color = Color.Blue,
                modifier = Modifier.clickable {
                    if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                errorMessage = if (task.isSuccessful) {
                                    "Password reset email sent to $email."
                                } else {
                                    "Error: ${task.exception?.message}"
                                }
                            }
                    } else {
                        errorMessage = "Please enter your email address to reset your password."
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navigator.push(TechRegisterScreen()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}