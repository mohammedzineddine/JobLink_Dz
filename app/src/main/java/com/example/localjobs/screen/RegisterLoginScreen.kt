package com.example.localjobs.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.R
import com.example.localjobs.screen.tech.TechLoginScreen
import com.example.localjobs.screen.technician.TechRegisterScreen
import com.example.localjobs.screen.user.UserLoginScreen
import com.example.localjobs.screen.user.UserRegisterScreen

class RegisterLoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp), // Reduced vertical padding
                verticalArrangement = Arrangement.Top, // Align content to the top
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo or Image
                Image(
                    painter = painterResource(id = R.drawable.decide_re),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(250.dp) // Reduced size for better spacing
                        .padding(top = 8.dp) // Added minimal top padding
                )

                Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                // Heading
                Text(
                    text = "Local Job Matcher",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                // Role Selection Buttons
                Text(
                    text = "Select Your Role:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // User Role Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp) // Increased height for better visibility
                        .clickable { navigator.push(UserRoleScreen()) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You're Looking for an Artisan?",
                                fontSize = 22.sp, // Larger font size
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Find and hire the best local Artisans for your needs.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "User Icon",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                // Technician Role Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp) // Increased height for better visibility
                        .clickable { navigator.push(TechnicianRoleScreen()) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You're an Artisan?",
                                fontSize = 22.sp, // Larger font size
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Showcase your skills and connect with clients in your area.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.technician),
                            contentDescription = "Technician Icon",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                // Informational Footer
                Text(
                    text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

            }
        }
    }
}

class UserRoleScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.undraw_people), // Replace with your logo
                contentDescription = "App Logo",
                modifier = Modifier.size(270.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Text
            Text(
                text = "Local Jobs",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction Text
            Text(
                text = "Get started Now:",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = { navigator.push(UserRegisterScreen()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            OutlinedButton(
                onClick = { navigator.push(UserLoginScreen()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Text
            Text(
                text = "Already have an account? Login now!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

class TechnicianRoleScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo or Header Image
            Image(
                painter = painterResource(id = R.drawable.undraw_factory), // Replace with actual image resource
                contentDescription = "Small Business Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Text(
                text = "Technician Role",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction Text
            Text(
                text = "Access your account or register your business:",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = { navigator.replace(TechRegisterScreen()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button (OutlinedButton)
            OutlinedButton(
                onClick = { navigator.push(TechLoginScreen()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary,
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Text
            Text(
                text = "Already registered? Login to manage your business.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}