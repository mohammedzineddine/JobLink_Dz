package com.example.localjobs.jobPost

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.example.localjobs.supabase.SupabaseApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class PostJobViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    val context = getApplication<Application>().applicationContext
    private val database = FirebaseDatabase.getInstance().getReference("jobs")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    var title = mutableStateOf("")
    var description = mutableStateOf("")
    var location = mutableStateOf("")
    var phoneNumber = mutableStateOf("")
    var imageUri = mutableStateOf<Uri?>(null)
    var imageUrl = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    // Retrofit instance for Supabase
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gdyegejqcfitkvdmupzf.supabase.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val supabaseApi = retrofit.create(SupabaseApiService::class.java)

    fun uploadImage() {
        imageUri.value?.let { uri ->
            isLoading.value = true
            val file = uriToFile(uri) ?: return
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            viewModelScope.launch {
                try {
                    val response = supabaseApi.uploadImage(file.name, body).execute()
                    if (response.isSuccessful) {
                        imageUrl.value = "https://gdyegejqcfitkvdmupzf.supabase.co/storage/v1/object/public/jobs/${file.name}"
                        Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading.value = false
                }
            }
        }
    }

    fun postJob(navigator: Navigator?) {
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.value.isEmpty() || description.value.isEmpty() || location.value.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val jobId = database.push().key
        val job = mapOf(
            "userId" to currentUser.uid,
            "title" to title.value,
            "description" to description.value,
            "location" to location.value,
            "phoneNumber" to phoneNumber.value,
            "status" to "Open",
            "imageUrl" to (imageUrl.value ?: "")
        )

        jobId?.let {
            database.child(it).setValue(job)
                .addOnSuccessListener {
                    Toast.makeText(context, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                    navigator?.pop()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to Post Job: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = "job_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}