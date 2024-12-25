package com.example.localjobs.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localjobs.Data.Job
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobListViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance().getReference("jobs")
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> get() = _jobs
    private var valueEventListener: ValueEventListener? = null

    init {
        fetchJobs()
    }

    private fun fetchJobs() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobList = mutableListOf<Job>()
                for (jobSnapshot in snapshot.children) {
                    val job = jobSnapshot.getValue(Job::class.java)
                    if (job != null) {
                        jobList.add(job)
                    }
                }
                _jobs.value = jobList // Update the state with fetched jobs
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error
                println("Error fetching data: ${error.message}")
            }
        }

        db.addValueEventListener(valueEventListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up the listener to prevent memory leaks
        valueEventListener?.let { db.removeEventListener(it) }
    }
}
