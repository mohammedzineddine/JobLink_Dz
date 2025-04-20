package com.example.localjobs.jobPost

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import cafe.adriel.voyager.navigator.Navigator
import com.example.localjobs.Data.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

private const val TAG = "PostJobViewModel"

class PostJobViewModel(application: Application) : AndroidViewModel(application) {
    val context = application

    // Job details
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val location = mutableStateOf("")
    val phoneNumber = mutableStateOf("")
    val status = mutableStateOf("Open")
    val skills = mutableStateOf("Not Specified")
    val estimatedTime = mutableStateOf("Not Specified")

    // Location data
    val latitude = mutableStateOf("")
    val longitude = mutableStateOf("")

    // Image related
    val imageUri = mutableStateOf<Uri?>(null)
    val isLoading = mutableStateOf(false)
    private var imageUrl = ""

    fun uploadImage() {
        if (imageUri.value == null) {
            Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading.value = true
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("job_images/${System.currentTimeMillis()}.jpg")

        val file = imageUri.value?.let { uriToFile(context, it) }
        if (file != null) {
            val uploadTask = imageRef.putFile(Uri.fromFile(file))
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    imageUrl = task.result.toString()
                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to upload image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isLoading.value = false
            Toast.makeText(context, "Failed to prepare image file", Toast.LENGTH_SHORT).show()
        }
    }

    fun postJob(navigator: Navigator?) {
        // Check if all required fields are filled
        if (title.value.isBlank() || description.value.isBlank() || location.value.isBlank() || phoneNumber.value.isBlank()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading.value = true

        // Create a unique job ID
        val jobId = FirebaseDatabase.getInstance().getReference("jobs").push().key
        if (jobId == null) {
            isLoading.value = false
            Toast.makeText(context, "Failed to create job ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Process coordinates with proper error handling
        val lat = try {
            latitude.value.toDouble()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting latitude: ${e.message}, value: '${latitude.value}'")
            0.0
        }

        val lng = try {
            longitude.value.toDouble()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting longitude: ${e.message}, value: '${longitude.value}'")
            0.0
        }

        Log.d(TAG, "Saving job with coordinates: lat=$lat, lng=$lng")

        // Create job object
        val job = Job(
            id = jobId,
            title = title.value,
            description = description.value,
            location = location.value,
            phoneNumber = phoneNumber.value,
            status = status.value,
            skills = skills.value,
            estimatedTime = estimatedTime.value,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            latitude = lat,
            longitude = lng
        )

        // Save to Firebase
        FirebaseDatabase.getInstance().getReference("jobs").child(jobId).setValue(job)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    // Verify the data was saved correctly
                    FirebaseDatabase.getInstance().getReference("jobs").child(jobId).get()
                        .addOnSuccessListener { snapshot ->
                            val savedJob = snapshot.getValue(Job::class.java)
                            Log.d(TAG, "Saved job coordinates: lat=${savedJob?.latitude}, lng=${savedJob?.longitude}")
                        }

                    Toast.makeText(context, "Job posted successfully!", Toast.LENGTH_SHORT).show()
                    navigator?.pop()
                } else {
                    Toast.makeText(context, "Failed to post job: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}