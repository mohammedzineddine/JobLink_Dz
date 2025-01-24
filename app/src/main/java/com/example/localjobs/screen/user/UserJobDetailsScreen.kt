package com.example.localjobs.screen.user



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

        }
    }
}