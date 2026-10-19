package com.example.tshwaneconnect

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

class EventStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "TLF_EVENTS_STORAGE",
        Context.MODE_PRIVATE
    )

    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val EVENTS_KEY = "saved_events"
    }

    // CURRENTLY LOGGED-IN USER
    private fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    // CHECK WHETHER THE USER CREATED THIS EVENT
    fun isEventOwner(eventId: String): Boolean {

        val userId = currentUserId() ?: return false

        val event = getEventById(eventId)
            ?: return false

        return event.createdBy == userId
    }

    // SAVE A NEW EVENT
    fun saveEvent(event: TlfEvent): Boolean {

        val userId = currentUserId()
            ?: return false

        val events = getAllEvents().toMutableList()

        // Prevent duplicate event IDs
        if (events.any { it.id == event.id }) {
            return false
        }

        // Record the event creator
        val eventToSave = event.copy(
            createdBy = userId
        )

        events.add(eventToSave)

        return saveAllEvents(events)
    }

    // GET ALL EVENTS
    fun getAllEvents(): List<TlfEvent> {

        val jsonString = preferences.getString(
            EVENTS_KEY,
            "[]"
        ) ?: "[]"

        return try {

            val jsonArray = JSONArray(jsonString)
            val events = mutableListOf<TlfEvent>()

            for (i in 0 until jsonArray.length()) {

                val jsonObject = jsonArray.getJSONObject(i)

                val event = TlfEvent(
                    id = jsonObject.getString("id"),
                    title = jsonObject.getString("title"),
                    description = jsonObject.getString("description"),
                    date = jsonObject.getString("date"),
                    startTime = jsonObject.getString("startTime"),
                    endTime = jsonObject.getString("endTime"),
                    venue = jsonObject.getString("venue"),
                    address = jsonObject.getString("address"),
                    imageUri = jsonObject.optString(
                        "imageUri",
                        ""
                    ).ifBlank { null },
                    createdAt = jsonObject.optLong(
                        "createdAt",
                        System.currentTimeMillis()
                    ),
                    createdBy = jsonObject.optString(
                        "createdBy",
                        ""
                    )
                )

                events.add(event)
            }

            events.sortedWith(
                compareBy<TlfEvent> { it.date }
                    .thenBy { it.startTime }
            )

        } catch (_: Exception) {
            emptyList()
        }
    }

    // GET ONE EVENT BY ID
    fun getEventById(eventId: String): TlfEvent? {

        return getAllEvents().find {
            it.id == eventId
        }
    }

    // UPDATE AN EXISTING EVENT
    fun updateEvent(updatedEvent: TlfEvent): Boolean {

        val events = getAllEvents().toMutableList()

        val eventIndex = events.indexOfFirst {
            it.id == updatedEvent.id
        }

        if (eventIndex == -1) {
            return false
        }

        val originalEvent = events[eventIndex]

        // Only the original creator can edit
        if (!isEventOwner(originalEvent.id)) {
            return false
        }

        // Preserve creator and creation date
        val eventToSave = updatedEvent.copy(
            createdAt = originalEvent.createdAt,
            createdBy = originalEvent.createdBy
        )

        events[eventIndex] = eventToSave

        return saveAllEvents(events)
    }

    // DELETE AN EVENT
    fun deleteEvent(eventId: String): Boolean {

        // Only the creator can delete
        if (!isEventOwner(eventId)) {
            return false
        }

        val existingEvents = getAllEvents()

        val updatedEvents = existingEvents.filter {
            it.id != eventId
        }

        return saveAllEvents(updatedEvents)
    }

    // WRITE EVENTS TO LOCAL STORAGE
    private fun saveAllEvents(events: List<TlfEvent>): Boolean {

        return try {

            val jsonArray = JSONArray()

            events.forEach { event ->

                val jsonObject = JSONObject().apply {
                    put("id", event.id)
                    put("title", event.title)
                    put("description", event.description)
                    put("date", event.date)
                    put("startTime", event.startTime)
                    put("endTime", event.endTime)
                    put("venue", event.venue)
                    put("address", event.address)
                    put("imageUri", event.imageUri ?: "")
                    put("createdAt", event.createdAt)
                    put("createdBy", event.createdBy)
                }

                jsonArray.put(jsonObject)
            }

            preferences.edit()
                .putString(
                    EVENTS_KEY,
                    jsonArray.toString()
                )
                .commit()

        } catch (_: Exception) {
            false
        }
    }
}
