package com.example.localjobs.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localjobs.Data.Job
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> get() = _jobs

    init {
        fetchJobs()
    }

    private fun fetchJobs() {
        viewModelScope.launch {
            db.collection("jobs").get()
                .addOnSuccessListener { result ->
                    val jobList = result.map { document ->
                        document.toObject(Job::class.java).copy(id = document.id)
                    }
                    _jobs.value = jobList
                }
                .addOnFailureListener { exception ->
                    // Handle error

                }
        }
    }
}
