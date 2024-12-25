package com.example.localjobs.JobPost

import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PostJobScreen : Screen {
    @Composable
    override fun Content() {
        PostJobContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobContent() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance().getReference("jobs")
    val storage = FirebaseStorage.getInstance().reference
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val navigator = LocalNavigator.current // Get the current navigator

    // Handle image/video pick
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            videoUri = it
        }
    }

    // Function to upload image or video to Firebase Storage
    fun uploadMediaToFirebase(uri: Uri, mediaType: String, onSuccess: (String) -> Unit) {
        val mediaRef: StorageReference = storage.child("media/$mediaType/${uri.lastPathSegment}")
        mediaRef.putFile(uri)
            .addOnSuccessListener {
                mediaRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload $mediaType", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Job") },
                navigationIcon = {
                    IconButton(onClick = { navigator?.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Post a Job",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Job Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Job Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Job Location (Google Maps URL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                // Image or Video Upload
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    Button(onClick = { pickImageLauncher.launch("image/*") }) {
                        Text("Pick Image")
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Button(onClick = { pickVideoLauncher.launch("video/*") }) {
                        Text("Pick Video")
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                if (imageUri != null) {
                    item {
                        Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = "Selected Image")
                    }
                }

                if (videoUri != null) {
                    item {
                        Text("Video Selected: ${videoUri.toString()}")
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    Button(
                        onClick = {
                            if (currentUser != null) {
                                val jobId = database.push().key // Generate unique job ID
                                val mediaUrls = mutableMapOf<String, String>()

                                // Upload Image if selected
                                imageUri?.let {
                                    uploadMediaToFirebase(it, "image") { url ->
                                        mediaUrls["image"] = url
                                    }
                                }

                                // Upload Video if selected
                                videoUri?.let {
                                    uploadMediaToFirebase(it, "video") { url ->
                                        mediaUrls["video"] = url
                                    }
                                }

                                // Save job details with media URLs
                                jobId?.let {
                                    val job = mapOf(
                                        "userId" to currentUser.uid,
                                        "title" to title.text,
                                        "description" to description.text,
                                        "location" to location.text,
                                        "status" to "Open",
                                        "mediaUrls" to mediaUrls
                                    )
                                    database.child(it).setValue(job)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to Post Job", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Post Job")
                    }
                }
            }
        }
    )
}
