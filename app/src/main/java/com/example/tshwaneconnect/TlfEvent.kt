package com.example.tshwaneconnect

import java.util.UUID

data class TlfEvent(

    // Unique identifier for each event
    val id: String = UUID.randomUUID().toString(),

    // Basic event information
    val title: String,
    val description: String,

    // Event schedule
    val date: String,
    val startTime: String,
    val endTime: String,

    // Event location
    val venue: String,
    val address: String,

    // Optional event image
    val imageUri: String? = null,

    // Date and time the event was created
    val createdAt: Long = System.currentTimeMillis(),

    // Firebase UID of the staff member who created the event
    val createdBy: String = ""
)
