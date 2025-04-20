package com.example.localjobs.screen.user

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.localjobs.Data.Job
import com.example.localjobs.R
import com.example.localjobs.screen.MapScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private const val TAG = "UserJobDetailsScreen"

class UserJobDetailsScreen(private val job: Job) : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        var fullName by remember { mutableStateOf("User") }
        val userDatabase = FirebaseDatabase.getInstance().getReference("Users")
        var showDialog by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        // Variables to store the job's coordinates from Firebase
        var jobLatitude by remember { mutableStateOf(job.latitude) }
        var jobLongitude by remember { mutableStateOf(job.longitude) }

        // Log initial coordinates
        LaunchedEffect(Unit) {
            Log.d(TAG, "Initial coordinates from job object: lat=${job.latitude}, lng=${job.longitude}")
        }

        // Direct Firebase data access for debugging
        LaunchedEffect(job.id) {
            if (!job.id.isNullOrEmpty()) {
                Log.d(TAG, "Fetching job data directly for ID: ${job.id}")
                val jobsRef = FirebaseDatabase.getInstance().getReference("jobs").child(job.id)
                jobsRef.get().addOnSuccessListener { snapshot ->
                    // Debug raw Firebase data
                    val rawLat = snapshot.child("latitude").getValue()
                    val rawLng = snapshot.child("longitude").getValue()

                    Log.d(TAG, "Raw latitude from Firebase: $rawLat (${rawLat?.javaClass?.name})")
                    Log.d(TAG, "Raw longitude from Firebase: $rawLng (${rawLng?.javaClass?.name})")

                    // Try manual conversion
                    try {
                        val manualLat = (rawLat as? Number)?.toDouble() ?: rawLat.toString().toDoubleOrNull() ?: 0.0
                        val manualLng = (rawLng as? Number)?.toDouble() ?: rawLng.toString().toDoubleOrNull() ?: 0.0
                        Log.d(TAG, "Manually converted: lat=$manualLat, lng=$manualLng")

                        if (manualLat != 0.0 || manualLng != 0.0) {
                            jobLatitude = manualLat
                            jobLongitude = manualLng
                            Log.d(TAG, "Updated coordinates with manual conversion")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting coordinates: ${e.message}")
                    }

                    // Get the entire job object for debugging
                    val updatedJob = snapshot.getValue(Job::class.java)
                    updatedJob?.let {
                        Log.d(TAG, "Job object from Firebase: id=${it.id}, lat=${it.latitude}, lng=${it.longitude}")

                        // Update coordinates from job object if manual conversion failed
                        if (it.latitude != 0.0 || it.longitude != 0.0) {
                            jobLatitude = it.latitude
                            jobLongitude = it.longitude
                            Log.d(TAG, "Updated coordinates from job object")
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch job data: ${e.message}")
                }
            }
        }

        LaunchedEffect(currentUser) {
            currentUser?.let { userId ->
                userDatabase.child(userId).get().addOnSuccessListener { snapshot ->
                    fullName = snapshot.child("fullName").value as? String ?: "User"
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to load your data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            JobImage(painter = painterResource(id = R.drawable.interview_re))

            Spacer(modifier = Modifier.height(16.dp))

            JobTitle(title = job.title)

            Spacer(modifier = Modifier.height(8.dp))

            JobDescription(description = job.description)

            Spacer(modifier = Modifier.height(16.dp))

            JobDetailsCard(job = job, fullName = fullName)

            Spacer(modifier = Modifier.height(16.dp))

            // Location Button - Using coordinates from Firebase
            Button(
                onClick = {
                    // Log the coordinates being passed
                    Log.d(TAG, "Navigating to MapScreen with coordinates: lat=$jobLatitude, lng=$jobLongitude")

                    if (jobLatitude == 0.0 && jobLongitude == 0.0) {
                        // If coordinates are still 0.0, try using default Algiers coordinates
                        Toast.makeText(context, "No exact location available. Showing approximate area.", Toast.LENGTH_SHORT).show()
                        navigator?.push(MapScreen(36.7538, 3.0588)) // Default Algiers coordinates
                    } else {
                        Toast.makeText(context, "Opening map at job location", Toast.LENGTH_SHORT).show()
                        navigator?.push(MapScreen(jobLatitude, jobLongitude))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Green color
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "View Job Location", color = Color.White)
            }

            // Display coordinates for verification
            Text(
                text = if (jobLatitude != 0.0 || jobLongitude != 0.0)
                    "Location coordinates: ($jobLatitude, $jobLongitude)"
                else
                    "No precise coordinates available for this job",
                style = MaterialTheme.typography.bodyMedium,
                color = if (jobLatitude != 0.0 || jobLongitude != 0.0) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactButton(
                    text = "Call",
                    icon = Icons.Default.Call,
                    onClick = { handleCallAction(context, job.phoneNumber) },
                    modifier = Modifier.weight(1f)
                )

                ContactButton(
                    text = "Message",
                    icon = Icons.AutoMirrored.Filled.Message,
                    onClick = { handleMessageAction(context, job.phoneNumber) },
                    modifier = Modifier.weight(1f)
                )
            }

            val isPublisher = currentUser == job.userId
            if (isPublisher) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Job", color = Color.White)
                }

                if (showDialog) {
                    ConfirmDeleteDialog(
                        onConfirm = {
                            deleteJob(context, job.id)
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
                    )
                }
            }
        }
    }

    @Composable
    private fun JobImage(painter: Painter) {
        Image(
            painter = painter,
            contentDescription = "Job Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }

    @Composable
    private fun JobTitle(title: String) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    @Composable
    private fun JobDescription(description: String) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    @Composable
    private fun JobDetailsCard(job: Job, fullName: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Location", value = job.location)
                DetailRow(label = "Status", value = job.status)
                DetailRow(label = "Skills", value = job.skills)
                DetailRow(label = "Estimated Time", value = job.estimatedTime)
                DetailRow(label = "Posted by", value = fullName)
            }
        }
    }

    @Composable
    private fun DetailRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$label:", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
        }
    }

    @Composable
    private fun ContactButton(
        text: String,
        icon: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier.height(48.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color.White)
        }
    }

    @Composable
    private fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this job?") }
        )
    }

    private fun handleCallAction(context: Context, contactNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactNumber"))
        context.startActivity(intent)
    }

    private fun handleMessageAction(context: Context, contactNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$contactNumber"))
        context.startActivity(intent)
    }

    private fun deleteJob(context: Context, jobId: String?) {
        if (jobId.isNullOrEmpty()) {
            Toast.makeText(context, "Invalid job ID", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId)

        databaseRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Job deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete job", Toast.LENGTH_SHORT).show()
            }
        }
    }
}