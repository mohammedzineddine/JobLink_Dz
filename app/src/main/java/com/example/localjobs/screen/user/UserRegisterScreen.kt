package com.example.localjobs.screen.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
        var phoneNumber by remember { mutableStateOf("") }
        var dateOfBirth by remember { mutableStateOf("") }
        var sex by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isPolicyChecked by remember { mutableStateOf(false) }
        var isPasswordVisible by remember { mutableStateOf(false) }

        var isLoading by remember { mutableStateOf(false) }  // Track loading state

        // Wrap the entire content in a Scrollable Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling
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

            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
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

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Number Field
            // Phone Number Field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.algeria_flag), // Replace with your flag resource
                            contentDescription = "Country Flag",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Date of Birth Field
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = { Text("Date of Birth (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Date of Birth",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sex Field
            OutlinedTextField(
                value = sex,
                onValueChange = { sex = it },
                label = { Text("Sex (Male/Female)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "Sex",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Password",
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isPasswordVisible = !isPasswordVisible }
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Confirm Password",
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isPasswordVisible = !isPasswordVisible }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Policy Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isPolicyChecked,
                    onCheckedChange = { isPolicyChecked = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("You agree to our Privacy Policy")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message display
            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Display CircularProgressIndicator if loading
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Sign-Up Button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword && isPolicyChecked) {
                        isLoading = true  // Set loading to true when the button is clicked
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading =
                                    false  // Set loading to false once operation is complete
                                if (task.isSuccessful) {
                                    val userId = task.result.user?.uid
                                    if (userId != null) {
                                        // Save user to Firebase Database
                                        val user = mapOf(
                                            "fullName" to fullName,
                                            "email" to email,
                                            "phoneNumber" to phoneNumber,
                                            "dateOfBirth" to dateOfBirth,
                                            "sex" to sex,
                                            "password" to password
                                        )
                                        database.child(userId).setValue(user)
                                            .addOnCompleteListener { dbTask ->
                                                if (dbTask.isSuccessful) {
                                                    navigator.push(HomeScreen()) // Navigate to HomeScreen after successful registration
                                                } else {
                                                    errorMessage =
                                                        "Failed to save user data: ${dbTask.exception?.message}"
                                                }
                                            }
                                    }
                                } else {
                                    errorMessage = "Registration failed: ${task.exception?.message}"
                                }
                            }
                    } else {
                        errorMessage =
                            "Please fill in all fields correctly and agree to the policy."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isPolicyChecked && !isLoading // Disable button while loading
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navigator.push(UserLoginScreen()) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? Log in", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
