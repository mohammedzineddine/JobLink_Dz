package com.example.localjobs.jobPost

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

private const val TAG = "PostJobScreen"

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
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val scrollState = rememberScrollState()
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.imageUri.value = uri
    }

    // Function to convert address to coordinates with improved error handling
    fun geocodeAddress(address: String) {
        try {
            Log.d(TAG, "Geocoding address: $address")
            val geocoder = Geocoder(context, Locale.getDefault())

            // Handle geocoding differently based on Android version
            val addresses = geocoder.getFromLocationName(address, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                viewModel.latitude.value = location.latitude.toString()
                viewModel.longitude.value = location.longitude.toString()

                Log.d(TAG, "Geocoded coordinates: lat=${location.latitude}, lng=${location.longitude}")

                Toast.makeText(
                    context,
                    "Location coordinates updated from address",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.w(TAG, "Geocoder returned no results for address: $address")
                Toast.makeText(
                    context,
                    "Could not find coordinates for this address. Try being more specific.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in geocoding: ${e.message}", e)
            Toast.makeText(
                context,
                "Error getting coordinates: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Location permission granted, get current location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    viewModel.latitude.value = lat.toString()
                    viewModel.longitude.value = lng.toString()

                    Log.d(TAG, "Current location: lat=$lat, lng=$lng")

                    Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "Location services returned null location")
                    Toast.makeText(context, "Unable to fetch location. Please try again or enter address manually.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error getting location: ${e.message}", e)
                Toast.makeText(context, "Error fetching location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission denied. Cannot track job location.", Toast.LENGTH_SHORT).show()
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
                    .verticalScroll(scrollState)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button to get coordinates from the entered location
                    Button(
                        onClick = {
                            if (viewModel.location.value.isNotBlank()) {
                                geocodeAddress(viewModel.location.value)
                            } else {
                                Toast.makeText(context, "Please enter a location first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Map")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Coordinates from Address")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Button to use current location
                    Button(
                        onClick = {
                            // Request location permission if not granted
                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    // Permission already granted
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            val lat = location.latitude
                                            val lng = location.longitude
                                            viewModel.latitude.value = lat.toString()
                                            viewModel.longitude.value = lng.toString()
                                            Log.d(TAG, "Current location: lat=$lat, lng=$lng")
                                            Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Unable to fetch location. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                else -> {
                                    // Request permission
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use Current Location")
                    }
                }

                if (viewModel.latitude.value.isNotEmpty() && viewModel.longitude.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Location coordinates: (${viewModel.latitude.value}, ${viewModel.longitude.value})",
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "No coordinates set yet. Please use one of the location buttons above.",
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.phoneNumber.value,
                    onValueChange = { viewModel.phoneNumber.value = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    onClick = {
                        // Extra logging for debugging
                        Log.d(TAG, "Posting job with lat=${viewModel.latitude.value}, lng=${viewModel.longitude.value}")
                        viewModel.postJob(navigator)
                    },
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
        Log.e(TAG, "Failed to convert URI to file: ${e.message}")
        null
    }
}