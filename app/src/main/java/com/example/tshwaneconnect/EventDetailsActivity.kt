package com.example.tshwaneconnect

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var btnBackEventDetails: TextView
    private lateinit var btnBackToEvents: Button
    private lateinit var btnEditEvent: Button
    private lateinit var btnDeleteEvent: Button
    private lateinit var btnViewOnMaps: Button
    private lateinit var btnAddToCalendar: Button

    private lateinit var ivEventDetailsImage: ImageView

    private lateinit var tvEventDetailsTitle: TextView
    private lateinit var tvEventDetailsDate: TextView
    private lateinit var tvEventDetailsTime: TextView
    private lateinit var tvEventDetailsVenue: TextView
    private lateinit var tvEventDetailsAddress: TextView
    private lateinit var tvEventDetailsDescription: TextView

    private lateinit var eventStorage: EventStorage

    private var selectedEventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event_details)

        eventStorage = EventStorage(this)

        // CONNECT BUTTONS
        btnBackEventDetails = findViewById(R.id.btnBackEventDetails)
        btnBackToEvents = findViewById(R.id.btnBackToEvents)
        btnEditEvent = findViewById(R.id.btnEditEvent)
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent)
        btnViewOnMaps = findViewById(R.id.btnViewOnMaps)
        btnAddToCalendar = findViewById(R.id.btnAddToCalendar)

        // CONNECT IMAGE
        ivEventDetailsImage = findViewById(R.id.ivEventDetailsImage)

        // CONNECT EVENT INFORMATION
        tvEventDetailsTitle = findViewById(R.id.tvEventDetailsTitle)
        tvEventDetailsDate = findViewById(R.id.tvEventDetailsDate)
        tvEventDetailsTime = findViewById(R.id.tvEventDetailsTime)
        tvEventDetailsVenue = findViewById(R.id.tvEventDetailsVenue)
        tvEventDetailsAddress = findViewById(R.id.tvEventDetailsAddress)
        tvEventDetailsDescription = findViewById(R.id.tvEventDetailsDescription)

        // GET EVENT ID
        selectedEventId = intent.getStringExtra("event_id")

        // BACK BUTTONS
        btnBackEventDetails.setOnClickListener {
            finish()
        }

        btnBackToEvents.setOnClickListener {
            finish()
        }

        // EDIT EVENT
        btnEditEvent.setOnClickListener {
            openEditEvent()
        }

        // DELETE EVENT
        btnDeleteEvent.setOnClickListener {
            showDeleteConfirmation()
        }

        // GOOGLE MAPS
        btnViewOnMaps.setOnClickListener {
            openEventLocation()
        }

        tvEventDetailsAddress.setOnClickListener {
            openEventLocation()
        }

        // CALENDAR
        btnAddToCalendar.setOnClickListener {
            addEventToCalendar()
        }

        loadEventDetails()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::eventStorage.isInitialized &&
            ::tvEventDetailsTitle.isInitialized
        ) {
            loadEventDetails()
        }
    }

    // DISPLAY EVENT DETAILS AND CHECK OWNERSHIP
    private fun loadEventDetails() {

        val eventId = selectedEventId

        if (eventId.isNullOrBlank()) {
            showEventNotFound()
            return
        }

        val event = eventStorage.getEventById(eventId)

        if (event == null) {
            showEventNotFound()
            return
        }

        tvEventDetailsTitle.text = event.title

        tvEventDetailsDate.text =
            "Date: ${formatEventDate(event.date)}"

        tvEventDetailsTime.text =
            "Time: ${event.startTime} - ${event.endTime}"

        tvEventDetailsVenue.text =
            "Venue: ${event.venue}"

        tvEventDetailsAddress.text =
            "📍 Address: ${event.address}\nTap to view on Google Maps"

        tvEventDetailsDescription.text = event.description

        // CHECK IF CURRENT USER CREATED THE EVENT
        val isOwner = eventStorage.isEventOwner(eventId)

        if (isOwner) {
            btnEditEvent.visibility = View.VISIBLE
            btnDeleteEvent.visibility = View.VISIBLE
        } else {
            btnEditEvent.visibility = View.GONE
            btnDeleteEvent.visibility = View.GONE
        }

        // LOAD EVENT IMAGE
        ivEventDetailsImage.setImageDrawable(null)
        ivEventDetailsImage.visibility = View.GONE

        if (!event.imageUri.isNullOrBlank()) {
            try {
                ivEventDetailsImage.setImageURI(
                    Uri.parse(event.imageUri)
                )

                if (ivEventDetailsImage.drawable != null) {
                    ivEventDetailsImage.visibility = View.VISIBLE
                }

            } catch (_: Exception) {
                ivEventDetailsImage.visibility = View.GONE
            }
        }
    }

    // OPEN EVENT LOCATION IN GOOGLE MAPS
    private fun openEventLocation() {

        val eventId = selectedEventId ?: return
        val event = eventStorage.getEventById(eventId) ?: return

        val address = event.address.trim()

        if (address.isBlank()) {
            Toast.makeText(
                this,
                "No address has been provided for this event.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val encodedAddress = Uri.encode(address)

        val mapsUri = Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=$encodedAddress"
        )

        val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri)

        try {
            startActivity(mapsIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No application is available to open the map.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ADD EVENT TO CALENDAR
    private fun addEventToCalendar() {

        val eventId = selectedEventId ?: return
        val event = eventStorage.getEventById(eventId) ?: return

        try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.US
            ).apply {
                isLenient = false
            }

            val startDate = inputFormat.parse(
                "${event.date} ${event.startTime}"
            )

            val endDate = inputFormat.parse(
                "${event.date} ${event.endTime}"
            )

            if (startDate == null || endDate == null) {
                Toast.makeText(
                    this,
                    "Unable to read the event date or time.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val startCalendar = Calendar.getInstance().apply {
                time = startDate
            }

            val endCalendar = Calendar.getInstance().apply {
                time = endDate
            }

            // HANDLE EVENTS THAT FINISH AFTER MIDNIGHT
            if (!endCalendar.after(startCalendar)) {
                endCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val calendarIntent = Intent(
                Intent.ACTION_INSERT
            ).apply {
                data = CalendarContract.Events.CONTENT_URI

                putExtra(
                    CalendarContract.Events.TITLE,
                    event.title
                )

                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    event.description
                )

                putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    "${event.venue}, ${event.address}"
                )

                putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    startCalendar.timeInMillis
                )

                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    endCalendar.timeInMillis
                )
            }

            startActivity(calendarIntent)

        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No calendar application is available.",
                Toast.LENGTH_LONG
            ).show()

        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Unable to prepare this calendar event. Check its date and time.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // OPEN EDIT SCREEN - OWNER ONLY
    private fun openEditEvent() {

        val eventId = selectedEventId ?: return

        if (!eventStorage.isEventOwner(eventId)) {
            Toast.makeText(
                this,
                "Only the event creator can edit this event.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val editIntent = Intent(
            this,
            EditEventActivity::class.java
        ).apply {
            putExtra("event_id", eventId)
        }

        startActivity(editIntent)
    }

    // DELETE CONFIRMATION - OWNER ONLY
    private fun showDeleteConfirmation() {

        val eventId = selectedEventId ?: return

        if (!eventStorage.isEventOwner(eventId)) {
            Toast.makeText(
                this,
                "Only the event creator can delete this event.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Event?")
            .setMessage(
                "Are you sure you want to delete this event? " +
                        "This action cannot be undone."
            )
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                deleteSelectedEvent(eventId)
            }
            .show()
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(Color.rgb(180, 35, 24))
    }

    // DELETE EVENT
    private fun deleteSelectedEvent(eventId: String) {

        btnDeleteEvent.isEnabled = false

        val deletedSuccessfully =
            eventStorage.deleteEvent(eventId)

        if (deletedSuccessfully) {
            Toast.makeText(
                this,
                "Event deleted successfully.",
                Toast.LENGTH_LONG
            ).show()

            finish()

        } else {
            btnDeleteEvent.isEnabled = true

            Toast.makeText(
                this,
                "Unable to delete event. Only its creator can delete it.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // EVENT NOT FOUND
    private fun showEventNotFound() {

        Toast.makeText(
            this,
            "This event could not be found.",
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    // FORMAT EVENT DATE
    private fun formatEventDate(date: String): String {

        return try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).apply {
                isLenient = false
            }

            val parsedDate = inputFormat.parse(date)

            if (parsedDate == null) {
                date
            } else {
                SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(parsedDate)
            }

        } catch (_: Exception) {
            date
        }
    }
}
