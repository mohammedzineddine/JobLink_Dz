package com.example.localjobs.Data

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val status: String = "Open", // Default status is "Open"
    val imageUri: String? = null, // Nullable to handle jobs without images
    val videoUri: String? = null, // Nullable to handle jobs without videos
    val createdAt: Long = 0, // Timestamp for creation time
    val updatedAt: Long = 0 // Timestamp for the last update time
) {
    val skills: String=""
    val estimatedTime: String=""
    val preferences: String=""
}
