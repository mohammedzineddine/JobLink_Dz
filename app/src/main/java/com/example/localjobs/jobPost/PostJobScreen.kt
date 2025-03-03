package com.example.localjobs.jobPost

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream

class PostJobScreen : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        PostJobContent(viewModel = PostJobViewModel(context.applicationContext as android.app.Application))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobContent(viewModel: PostJobViewModel) {
    LocalContext.current
    val navigator = LocalNavigator.current
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.imageUri.value = uri
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
                OutlinedTextField(
                    value = viewModel.title.value,
                    onValueChange = { viewModel.title.value = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.description.value,
                    onValueChange = { viewModel.description.value = it },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.location.value,
                    onValueChange = { viewModel.location.value = it },
                    label = { Text("Job Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.phoneNumber.value,
                    onValueChange = { viewModel.phoneNumber.value = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { pickImageLauncher.launch("image/*") }) {
                    Text("Pick Image")
                }

                Spacer(modifier = Modifier.height(8.dp))

                viewModel.imageUri.value?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Button(onClick = { viewModel.uploadImage() }) {
                    Text("Upload Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.postJob(navigator) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading.value
                ) {
                    Text("Post Job")
                }

                if (viewModel.isLoading.value) {
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