package com.example.localjobs.Screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRegisterScreen : Screen {

    @Composable
    override fun Content() {
        val auth = FirebaseAuth.getInstance()
        val navigator = LocalNavigator.currentOrThrow
        val database = FirebaseDatabase.getInstance().getReference("Users")

        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isPolicyChecked by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.profile_details),
                contentDescription = "App Logo",
                modifier = Modifier.size(230.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Create an Account", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name Field
            AnimatedVisibility(visible = true) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Full Name",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            AnimatedVisibility(visible = true) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password Field
            AnimatedVisibility(visible = true) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Password",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password Field
            AnimatedVisibility(visible = true) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Confirm Password",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Policy Checkbox
            AnimatedVisibility(visible = true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPolicyChecked,
                        onCheckedChange = { isPolicyChecked = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("You agree to our Privacy Policy")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message display
            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Sign-Up Button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword && isPolicyChecked) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = task.result.user?.uid
                                    if (userId != null) {
                                        // Save user to Firebase Database
                                        val user = mapOf(
                                            "fullName" to fullName,
                                            "email" to email,
                                            "password" to password
                                        )
                                        database.child(userId).setValue(user)
                                            .addOnCompleteListener { dbTask ->
                                                if (dbTask.isSuccessful) {
                                                    navigator.push(HomeScreen())
                                                } else {
                                                    errorMessage = "Failed to save user data: ${dbTask.exception?.message}"
                                                }
                                            }
                                    }
                                } else {
                                    errorMessage = "Registration failed: ${task.exception?.message}"
                                }
                            }
                    } else {
                        errorMessage = "Please fill in all fields correctly and agree to the policy."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isPolicyChecked
            ) {
                Text("Sign Up")
            }
        }
    }
}
