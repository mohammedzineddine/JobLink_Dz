package com.example.localjobs.Data

data class Job(
    val id: String = "",
    val userId: String="",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val latitude: Double = 0.0, // Add latitude field
    val longitude: Double = 0.0, // Add longitude field
    val status: String = "Open", // Default status is "Open"
    val imageUri: String? = null, // Nullable to handle jobs without images
    val videoUri: String? = null, // Nullable to handle jobs without videos
    val createdAt: Long = 0, // Timestamp for creation time
    val updatedAt: Long = 0, // Timestamp for the last update time
    val phoneNumber: String = "", // Added contact number field
    val userName: String = "", // Added user name field
    val skills: String = "", // Added skills field
    val estimatedTime: String = "", // Added estimated time field
    val preferences: String = "" // Added preferences field
) {

    val priority: Any
        get() {
            return when (status) {
                "Open" -> 1
                "In Progress" -> 2
                "Completed" -> 3
                else -> 4
            }
        }
}