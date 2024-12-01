package com.example.localjobs

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

fun testFirestore() {
    val db = FirebaseFirestore.getInstance()
    db.collection("jobs").get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                println("${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            println("Error getting documents: $exception")
        }

}

fun fetchUserData(userId: String, onResult: (String) -> Unit) {
    val database = FirebaseDatabase.getInstance().getReference("Users")
    database.child(userId).get().addOnSuccessListener { snapshot ->
        val fullName = snapshot.child("fullName").value as? String ?: "User"
        onResult(fullName)
    }
}
