package com.example.localjobs.screen.user

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
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
import com.example.localjobs.Data.Job
import com.example.localjobs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserJobDetailsScreen(private val job: Job) : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        var fullName by remember { mutableStateOf("User") }
        val userDatabase = FirebaseDatabase.getInstance().getReference("Users")
        var showDialog by remember { mutableStateOf(false) }


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
        ) {
            JobImage(painter = painterResource(id = R.drawable.interview_re))

            Spacer(modifier = Modifier.height(16.dp))

            JobTitle(title = job.title)

            Spacer(modifier = Modifier.height(8.dp))

            JobDescription(description = job.description)

            Spacer(modifier = Modifier.height(16.dp))

            JobDetailsCard(job = job, fullName = fullName)

            Spacer(modifier = Modifier.height(16.dp))

            ContactButtons(
                onCallClick = { handleCallAction(context, job.phoneNumber) },
                onMessageClick = { handleMessageAction(context, job.phoneNumber) }
            )

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
    private fun ContactButtons(onCallClick: () -> Unit, onMessageClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContactButton(
                text = "Call",
                icon = Icons.Default.Call,
                onClick = onCallClick
            )

            ContactButton(
                text = "Message",
                icon = Icons.AutoMirrored.Filled.Message,
                onClick = onMessageClick
            )
        }
    }

    @Composable
    private fun ContactButton(text: String, icon: ImageVector, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(48.dp)
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