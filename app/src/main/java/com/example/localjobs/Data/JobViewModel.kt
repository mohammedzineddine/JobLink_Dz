package com.example.localjobs.Data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("jobs")

    private val _job = MutableLiveData<Job?>()
    val job: LiveData<Job?> get() = _job

    fun fetchJob(jobId: String) {
        database.child(jobId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val job = snapshot.getValue(Job::class.java)
                _job.value = job
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load job", error.toException())
            }
        })
    }
}
