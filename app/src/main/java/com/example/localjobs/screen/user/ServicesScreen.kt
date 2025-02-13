package com.example.localjobs.screen.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.R
import com.example.localjobs.Screens.SettingsScreen

class ServicesScreen : Screen {
    @Composable
    override fun Content() {
        ServicesScreenContent()
    }
}

@Composable
fun ServicesScreenContent() {
    val services = listOf(
        "Satellite" to R.drawable.starlink,
        "Plumbing" to R.drawable.plumber,
        "Home Cleaning" to R.drawable.cleaner,
    )
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
        topBar = {
            DashboardTopBar(
                onSettingsClick = { navigator.push(SettingsScreen()) },
                onMapClick = { navigator.push(MapScreen()) }, // Navigate to the MapScreen
                onBackClick = { navigator.pop() }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                items(services) { service ->
                    ServiceCard(serviceName = service.first, imageRes = service.second)
                }
            }
        }
    )
}

@Composable
fun ServiceCard(serviceName: String, imageRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { /* Navigate to the service details screen */ }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = serviceName,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = serviceName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(onSettingsClick: () -> Unit, onMapClick: () -> Unit, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Services") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // Map Icon
            IconButton(onClick = onMapClick) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Current Location"
                )
            }
            // Settings Icon
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

