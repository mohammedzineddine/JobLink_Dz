package com.example.localjobs.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.Data.Job
import com.example.localjobs.screen.user.UserJobDetailsScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class MapScreen : Screen {
    @Composable
    override fun Content() {
        OSMDroidMap()
    }
}

@Composable
fun OSMDroidMap() {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var locationName by remember { mutableStateOf("No location selected") }
    var mapCenter by remember { mutableStateOf(GeoPoint(36.7538, 3.0588)) } // Default location (Algiers)
    var hasLocationPermission by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("All", "Hospital", "Restaurant", "Shop")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var jobLocations by remember { mutableStateOf<List<Job>>(emptyList()) }

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasLocationPermission = isGranted
        if (!isGranted) Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
    }

    // Fetch job data from Firebase
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Fetch jobs from Firebase
        val database = FirebaseDatabase.getInstance().getReference("jobs")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobs = mutableListOf<Job>()
                for (jobSnapshot in snapshot.children) {
                    val job = jobSnapshot.getValue(Job::class.java)
                    if (job != null) {
                        jobs.add(job)
                    }
                }
                jobLocations = jobs
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load jobs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun searchLocation(query: String, category: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName("$query $category", 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                mapCenter = GeoPoint(location.latitude, location.longitude)
                locationName = location.getAddressLine(0) ?: "Location found"
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateSuggestions(query: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(query, 5)
            if (addresses != null) {
                suggestions = addresses.mapNotNull { it.locality }
            }
        } catch (e: Exception) {
            suggestions = emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                    controller.apply {
                        setZoom(15.0)
                        setCenter(mapCenter)
                    }
                }
            },
            update = { mapView ->
                mapView.controller.setCenter(mapCenter)
                mapView.overlays.clear()

                // Add job location markers
                jobLocations.forEach { job ->
                    val marker = Marker(mapView)
                    val location = GeoPoint(job.latitude, job.longitude)
                    marker.position = jobLocations.firstOrNull()?.let { location } ?: mapCenter
                    marker.title = job.title
                    marker.snippet = job.location
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.setOnMarkerClickListener { _, _ ->
                        navigator.push(UserJobDetailsScreen(job))
                        true
                    }
                    mapView.overlays.add(marker)
                }

                // Add a marker for the searched location
                val searchedMarker = Marker(mapView)
                searchedMarker.position = mapCenter
                searchedMarker.title = "Selected Location"
                mapView.overlays.add(searchedMarker)

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            updateSuggestions(it.text)
                        },
                        placeholder = { Text("Search here") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { searchLocation(searchQuery.text, selectedCategory) }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(50)
                    )
                    DropdownMenu(
                        expanded = suggestions.isNotEmpty(),
                        onDismissRequest = { suggestions = emptyList() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    searchQuery = TextFieldValue(suggestion)
                                    suggestions = emptyList()
                                    searchLocation(suggestion, selectedCategory)
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (hasLocationPermission) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            mapCenter = GeoPoint(location.latitude, location.longitude)
                        } else {
                            Toast.makeText(context, "Unable to fetch current location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "Current Location")
        }
    }
}