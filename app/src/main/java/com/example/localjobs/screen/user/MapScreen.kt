package com.example.localjobs.screen.user

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

public class MapScreen : Screen {
    @Composable
    override fun Content() {
        OSMDroidMap()
    }
}

@Composable
fun OSMDroidMap() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var locationName by remember { mutableStateOf("No location selected") }
    var mapCenter by remember { mutableStateOf(GeoPoint(36.7538, 3.0588)) } // Default location (Algiers)
    var hasLocationPermission by remember { mutableStateOf(false) }

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Permission launcher for location access
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasLocationPermission = isGranted
        if (!isGranted) Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Function to fetch current location
    fun getCurrentLocation() {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mapCenter = GeoPoint(location.latitude, location.longitude)
                    locationName = "Current Location: (${location.latitude}, ${location.longitude})"
                } else {
                    // Request location updates if last location is null
                    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 10000
                        fastestInterval = 5000
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                            val location = locationResult.lastLocation
                            if (location != null) {
                                mapCenter = GeoPoint(location.latitude, location.longitude)
                                locationName = "Current Location: (${location.latitude}, ${location.longitude})"
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                    }, null)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to search for a location
    fun searchLocation(query: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                mapCenter = GeoPoint(location.latitude, location.longitude)
                locationName = location.getAddressLine(0) ?: "Location found"
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching location", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Map View in the Background
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
                val marker = Marker(mapView)
                marker.position = mapCenter
                marker.title = "Selected Location"
                mapView.overlays.clear()
                mapView.overlays.add(marker)
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp) // Push the search bar down
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(50), // Highly rounded corners
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search here") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { searchLocation(searchQuery.text) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Inner padding
                    shape = RoundedCornerShape(50) // Match the Surface rounding
                )
            }
        }

        // Floating Button for Current Location
        FloatingActionButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Current Location"
            )
        }
    }
}