
package com.example.tshwaneconnect

import java.util.UUID

data class TlfCase(
    val id: String = UUID.randomUUID().toString(),
    val reference: String,
    val programme: String,
    val summary: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)
