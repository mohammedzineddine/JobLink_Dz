package com.example.localjobs.screen.technician

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.Data.Job
import com.example.localjobs.Screens.SettingsScreen
import com.example.localjobs.di.JobListViewModel
import com.example.localjobs.screen.introScreen
import com.example.localjobs.screen.user.HomeScreen
import com.example.localjobs.screen.user.HomeWidget
import com.example.localjobs.screen.user.LogoutButton
import com.example.localjobs.screen.user.MapScreen
import com.example.localjobs.screen.user.ProfileSettingsScreen
import com.example.localjobs.screen.user.ServicesScreen
import com.example.localjobs.screen.user.ServicesWidget
import com.example.localjobs.screen.user.UserJobDetailsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class HomeTch : HomeScreen() {

    @Composable
    override fun Content() {
        HomeTchContent()
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun HomeTchContent() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        var exitFlag by remember { mutableStateOf(false) }
        var fullName by remember { mutableStateOf("") }
        val viewModel: JobListViewModel = koinViewModel()
        val jobs by viewModel.jobs.collectAsState(initial = emptyList())
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Fetch the user's full name and role from Firebase
        LaunchedEffect(currentUser) {
            currentUser?.uid?.let { userId ->
                val database = FirebaseDatabase.getInstance().getReference("Technicians")
                database.child(userId).get().addOnSuccessListener { snapshot ->
                    fullName = snapshot.child("fullName").value as? String ?: "Technician"
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to load your data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle back press for exit
        BackHandler {
            if (exitFlag) {
                navigator.popUntilRoot()
            } else {
                exitFlag = true
                coroutineScope.launch {
                    delay(2000)
                    exitFlag = false
                }
            }
        }

        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            },
            content = { paddingValues ->
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                    }, label = ""
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeContent(
                            modifier = Modifier.padding(paddingValues),
                            fullName = fullName,
                            jobs = jobs,
                            onJobClick = { job -> navigator.push(UserJobDetailsScreen(job))},
                            onProfileClick = { navigator.push(ProfileSettingsScreen()) },
                            onLogoutClick = { navigator.replace(introScreen()) },
                            onServicesClick = { navigator.push(ServicesScreen()) }
                        )
                        1 -> MapScreen().Content()
                        2 -> ProfileSettingsScreen().Content()
                        3 -> SettingsScreen().Content()
                    }
                }
            }
        )
    }

    @Composable
    fun HomeContent(
        modifier: Modifier,
        fullName: String,
        jobs: List<Job>,
        onJobClick: (Job) -> Unit,
        onProfileClick: () -> Unit,
        onLogoutClick: () -> Unit,
        onServicesClick: () -> Unit
    ) {
        var filter by remember { mutableStateOf("") }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Greeting Section with animation
            AnimatedVisibility(
                visible = true,
                enter = expandIn(tween(600)),
                exit = fadeOut(tween(400))
            ) {
                Text(
                    text = "Welcome $fullName!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Filter Section
            TextField(
                value = filter,
                onValueChange = { filter = it },
                label = { Text("Filter by skill or location") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dashboard Widgets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HomeWidget(
                    title = "Total Jobs",
                    value = jobs.size.toString(),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                ServicesWidget(
                    title = "Services",
                    value = "3",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onServicesClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Job List Section
            Text(
                text = "Jobs Near You",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(jobs.filter { job ->
                    job.title.contains(filter, ignoreCase = true) || job.location.contains(filter, ignoreCase = true)
                }) { job ->
                    JobCard(job = job, onClick = onJobClick)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            LogoutButton(onClick = onLogoutClick)
        }
    }

    @Composable
    fun JobCard(job: Job, onClick: (Job) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(job) }
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (job.priority == "High") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = job.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = job.description, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Location: ${job.location}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Status: ${job.status}", style = MaterialTheme.typography.bodySmall)
                if (job.priority == "High") {
                    Text(text = "High Priority", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}