package com.example.localjobs.screen.user

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.localjobs.Data.Job
import com.example.localjobs.R

class UserJobDetailsScreen(private val job: Job) : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current  // Get the context inside composable

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Job Image
            Image(
                painter = painterResource(id = R.drawable.interview_re), // Replace with actual image resource
                contentDescription = "Job Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(bottom = 16.dp)
            )

            // Job Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Job Description
            Text(
                text = job.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Gray
            )

            // Job Location
            Text(
                text = "Location: ${job.location}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Job Status
            Text(
                text = "Status: ${job.status}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Skills Required
            Text(
                text = "Skills: ${job.skills}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Estimated Time
            Text(
                text = "Estimated Time: ${job.estimatedTime}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Job Poster Information
            Text(
                text = "Posted by: ${job.userName}", // Assuming there's a userName field added to the Job class
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Contact Buttons (Example for calling or messaging)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { handleCallAction(context, job.phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Call")
                }

                Button(
                    onClick = { handleMessageAction(context, job.phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Message")
                }
            }
        }
    }

    private fun handleCallAction(context: Context, contactNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.phoneNumber}"))
        context.startActivity(intent)
    }

    private fun handleMessageAction(context: Context, contactNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${job.phoneNumber}"))
        context.startActivity(intent)
    }
}
