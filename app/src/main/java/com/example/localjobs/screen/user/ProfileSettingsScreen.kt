package com.example.localjobs.screen.user

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

public class ProfileSettingsScreen : Screen {
    @Composable
    override fun Content() {
        ProfileSettingsScreenContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreenContent() {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()

    // Firebase instances
    val currentUser = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance().getReference("Users")
    val storage = FirebaseStorage.getInstance().reference

    // Mutable states for user information
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrl by remember { mutableStateOf<String>("") }

    // Load user data from Firebase
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            database.child(userId).get().addOnSuccessListener { snapshot ->
                fullName = snapshot.child("fullName").value as? String ?: ""
                email = snapshot.child("email").value as? String ?: currentUser.email ?: ""
                password = snapshot.child("password").value as? String ?: ""
                profilePictureUrl = snapshot.child("profilePicture").value as? String ?: ""
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Profile Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) },
               /* navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                }*/

            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    ProfileSettingsForm(
                        fullName = fullName,
                        email = email,
                        password = password,
                        isPasswordVisible = isPasswordVisible,
                        profilePictureUrl = profilePictureUrl,
                        onFullNameChange = { fullName = it },
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onPasswordVisibilityChange = { isPasswordVisible = it },
                        onProfilePictureChange = { uri -> profilePictureUri = uri },
                        onSave = {
                            isLoading = true
                            coroutineScope.launch {
                                val userId = currentUser?.uid
                                if (userId != null) {
                                    // Upload profile picture if it's changed
                                    val pictureUrl = if (profilePictureUri != null) {
                                        uploadProfilePicture(userId, profilePictureUri!!)
                                    } else {
                                        profilePictureUrl
                                    }

                                    // Save data to Realtime Database
                                    val updates = mapOf(
                                        "fullName" to fullName,
                                        "email" to email,
                                        "profilePicture" to pictureUrl
                                    )

                                    database.child(userId).updateChildren(updates)
                                        .addOnSuccessListener {
                                            if (password.isNotEmpty()) {
                                                currentUser.updatePassword(password)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                                        navigator.pop()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                                navigator.pop()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnCompleteListener { isLoading = false }
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ProfileSettingsForm(
    fullName: String,
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    profilePictureUrl: String,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onProfilePictureChange: (Uri?) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Picture
        val painter: Painter = rememberImagePainter(profilePictureUrl)
        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clickable { onProfilePictureChange(Uri.EMPTY) }
        )

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
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

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = "Toggle Password Visibility",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onPasswordVisibilityChange(!isPasswordVisible) }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = fullName.isNotEmpty() && email.isNotEmpty()
        ) {
            Text("Save Changes")
        }
    }
}

private suspend fun uploadProfilePicture(userId: String, uri: Uri): String {
    val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    val profilePictureRef: StorageReference = storageRef.child("profilePictures/$userId.jpg")

    // Upload the profile picture to Firebase Storage
    val uploadTask = profilePictureRef.putFile(uri)
    val result = uploadTask.await()
    return result.metadata?.path.orEmpty()  // Return the file path as the URL
}