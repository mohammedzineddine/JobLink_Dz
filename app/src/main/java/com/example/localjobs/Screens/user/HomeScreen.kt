package com.example.localjobs.Screens.user

import UserLoginScreen
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
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
import com.example.localjobs.R
import com.example.localjobs.Screens.IntroScreen
import com.example.localjobs.Screens.SettingsScreen
import com.example.localjobs.di.JobListViewModel
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

@Composable
fun HomeScreenContent() {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()
    var exitFlag by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("user") }
    val viewModel: JobListViewModel = koinViewModel()
    val jobs by viewModel.jobs.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Fetch the user's full name from Firebase
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
            android.os.Process.killProcess(android.os.Process.myPid())
        } else {
            exitFlag = true
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                delay(2000)
                exitFlag = false
            }
        }
    }

    Scaffold(
        topBar = { DashboardTopBar(onSettingsClick = { navigator.push(SettingsScreen()) }) },
        content = { paddingValues ->
            DashboardContent(
                modifier = Modifier.padding(paddingValues),
                fullName = fullName,
                jobs = jobs,
                onJobClick = { job -> navigator.push(UserJobDetailsScreen(job)) },
                onProfileClick = { /* Navigate to Profile Screen */ },
                onLogoutClick = { navigator.replace(IntroScreen()) }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
    )
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    fullName: String,
    jobs: List<Job>,
    onJobClick: (Job) -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Greeting Section with animation
        AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.expandIn(tween(600)),
            exit = androidx.compose.animation.fadeOut(tween(400))
        ) {
            Text(
                text = "Welcome back, $fullName!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Dashboard Widgets (Removed Saved Jobs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardWidget(
                title = "Total Jobs",
                value = jobs.size.toString(),
                color = MaterialTheme.colorScheme.primaryContainer
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
fun DashboardWidget(title: String, value: String, color: Color) {
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
            Text(text = job.title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = job.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = job.location, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(text = "Logout", color = MaterialTheme.colorScheme.onErrorContainer)
    }
}
