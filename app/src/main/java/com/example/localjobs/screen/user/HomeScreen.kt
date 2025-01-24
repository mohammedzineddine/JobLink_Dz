package com.example.localjobs.screen.user

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.localjobs.Data.Job
import com.example.localjobs.JobPost.PostJobScreen
import com.example.localjobs.R
import com.example.localjobs.Screens.SettingsScreen
import com.example.localjobs.di.JobListViewModel
import com.example.localjobs.screen.introScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreenContent()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreenContent() {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()
    var exitFlag by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    val viewModel: JobListViewModel = koinViewModel()
    val jobs by viewModel.jobs.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Fetch the user's full name and role from Firebase
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            val database = FirebaseDatabase.getInstance().getReference("Users")
            database.child(userId).get().addOnSuccessListener { snapshot ->
                fullName = snapshot.child("fullName").value as? String ?: "User"
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.push(PostJobScreen()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Post a Job")
            }
        },
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
                    fadeIn(animationSpec = tween(400)) with  fadeOut(animationSpec = tween(400))
                }, label = ""
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeContent(
                        modifier = Modifier.padding(paddingValues),
                        fullName = fullName,
                        jobs = jobs,
                        onJobClick = { job -> navigator.push(UserJobDetailsScreen(job)) },
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
    modifier: Modifier = Modifier,
    fullName: String,
    jobs: List<Job>,
    onJobClick: (Job) -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onServicesClick: () -> Unit
) {
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

        // Account Management Section
        Text(
            text = "Account Management",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ProfileCard(fullName = fullName, onClick = onProfileClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Job List Section with transition
        Text(
            text = "Jobs Near You",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(jobs) { job ->
                JobCard(job = job, onClick = onJobClick)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        LogoutButton(onClick = onLogoutClick)
    }
}

@Composable
fun ServicesWidget(title: String, value: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.80f)
            .height(100.dp)
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun HomeWidget(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.45f)
            .height(100.dp)
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ProfileCard(fullName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.male_avatar),
                contentDescription = "User Profile",
                modifier = Modifier.size(70.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
                Text(
                    text = "Edit your personal details and preferences",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun JobCard(job: Job, onClick: (Job) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(job) }
            .padding(vertical = 10.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = job.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = job.description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Location: ${job.location}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = "Logout", color = Color.White)
    }
}