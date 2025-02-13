package com.example.localjobs.jobPost

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import com.example.localjobs.supabase.SupabaseApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class PostJobScreen : Screen {
    @Composable
    override fun Content() {
        PostJobContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobContent() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance().getReference("jobs")

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Initialize Retrofit
    val retrofit = Retrofit.Builder()
        .baseUrl("https://gdyegejqcfitkvdmupzf.supabase.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val supabaseApi = retrofit.create(SupabaseApiService::class.java)

    // File picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    fun uploadImage() {
        if (imageUri == null) {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        val file = uriToFile(context, imageUri!!)
        if (file == null) {
            Toast.makeText(context, "Failed to get file", Toast.LENGTH_SHORT).show()
            isLoading = false
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call = supabaseApi.uploadImage(file.name, body)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                isLoading = false
                if (response.isSuccessful) {
                    imageUrl = "https://gdyegejqcfitkvdmupzf.supabase.co/storage/v1/object/public/jobs/${file.name}"
                    Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun postJob() {
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val jobId = database.push().key
        val job = mapOf(
            "userId" to currentUser.uid,
            "title" to title,
            "description" to description,
            "location" to location,
            "phoneNumber" to phoneNumber,
            "status" to "Open",
            "imageUrl" to (imageUrl ?: "")
        )

        jobId?.let {
            database.child(it).setValue(job)
                .addOnSuccessListener {
                    Toast.makeText(context, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                    navigator?.pop()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to Post Job: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Job Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Job Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.isEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Job Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Job Location") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = location.isEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = phoneNumber.isEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pick Image
                Button(onClick = { pickImageLauncher.launch("image/*") }) {
                    Text("Pick Image")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display selected image
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Upload Image Button
                Button(onClick = { uploadImage() }) {
                    Text("Upload Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Post Job Button
                Button(
                    onClick = { postJob() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Post Job")
                }

                // Loading Spinner
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    )
}

@SuppressLint("Recycle")
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val fileName = "job_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file
    } catch (e: Exception) {
        Log.e("Supabase", "Failed to convert URI to file: ${e.message}")
        null
    }
}
