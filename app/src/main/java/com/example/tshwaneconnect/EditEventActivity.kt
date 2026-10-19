package com.example.tshwaneconnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditEventActivity : AppCompatActivity() {

    private lateinit var btnBackEditEvent: TextView

    private lateinit var etEditEventTitle: EditText
    private lateinit var etEditEventDescription: EditText
    private lateinit var etEditEventVenue: EditText
    private lateinit var etEditEventAddress: EditText

    private lateinit var btnEditEventDate: Button
    private lateinit var btnEditStartTime: Button
    private lateinit var btnEditEndTime: Button
    private lateinit var btnSaveEventChanges: Button
    private lateinit var btnCancelEditEvent: Button

    private lateinit var eventStorage: EventStorage

    private var originalEvent: TlfEvent? = null

    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""

    private var isSaving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_event)

        eventStorage = EventStorage(this)

        // Connect XML components
        btnBackEditEvent =
            findViewById(R.id.btnBackEditEvent)

        etEditEventTitle =
            findViewById(R.id.etEditEventTitle)

        etEditEventDescription =
            findViewById(R.id.etEditEventDescription)

        etEditEventVenue =
            findViewById(R.id.etEditEventVenue)

        etEditEventAddress =
            findViewById(R.id.etEditEventAddress)

        btnEditEventDate =
            findViewById(R.id.btnEditEventDate)

        btnEditStartTime =
            findViewById(R.id.btnEditStartTime)

        btnEditEndTime =
            findViewById(R.id.btnEditEndTime)

        btnSaveEventChanges =
            findViewById(R.id.btnSaveEventChanges)

        btnCancelEditEvent =
            findViewById(R.id.btnCancelEditEvent)

        // Load the existing event
        val eventId = intent.getStringExtra("event_id")

        if (eventId.isNullOrBlank()) {
            showEventNotFound()
            return
        }

        originalEvent = eventStorage.getEventById(eventId)

        if (originalEvent == null) {
            showEventNotFound()
            return
        }

        populateEventFields(originalEvent!!)

        // Restore edits if Android recreated this screen
        if (savedInstanceState != null) {

            selectedDate = savedInstanceState.getString(
                "selectedDate",
                selectedDate
            )

            selectedStartTime = savedInstanceState.getString(
                "selectedStartTime",
                selectedStartTime
            )

            selectedEndTime = savedInstanceState.getString(
                "selectedEndTime",
                selectedEndTime
            )

            updateDateTimeButtons()
        }

        // Navigation
        btnBackEditEvent.setOnClickListener {
            finish()
        }

        btnCancelEditEvent.setOnClickListener {
            finish()
        }

        // Date picker
        btnEditEventDate.setOnClickListener {
            showDatePicker()
        }

        // Start time picker
        btnEditStartTime.setOnClickListener {
            showTimePicker(true)
        }

        // End time picker
        btnEditEndTime.setOnClickListener {
            showTimePicker(false)
        }

        // Save changes
        btnSaveEventChanges.setOnClickListener {
            saveEventChanges()
        }
    }

    private fun populateEventFields(event: TlfEvent) {

        etEditEventTitle.setText(event.title)

        etEditEventDescription.setText(
            event.description
        )

        etEditEventVenue.setText(event.venue)

        etEditEventAddress.setText(event.address)

        selectedDate = event.date
        selectedStartTime = event.startTime
        selectedEndTime = event.endTime

        updateDateTimeButtons()
    }

    private fun updateDateTimeButtons() {

        btnEditEventDate.text =
            if (selectedDate.isBlank()) {
                "Select Date"
            } else {
                selectedDate
            }

        btnEditStartTime.text =
            if (selectedStartTime.isBlank()) {
                "Select Start Time"
            } else {
                selectedStartTime
            }

        btnEditEndTime.text =
            if (selectedEndTime.isBlank()) {
                "Select End Time"
            } else {
                selectedEndTime
            }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        // Open the picker at the existing event date
        try {
            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            dateFormat.isLenient = false

            val parsedDate =
                dateFormat.parse(selectedDate)

            if (parsedDate != null) {
                calendar.time = parsedDate
            }

        } catch (_: Exception) {
            // Use today's date if parsing fails
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                selectedDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )

                updateDateTimeButtons()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(isStartTime: Boolean) {

        val currentTime = if (isStartTime) {
            selectedStartTime
        } else {
            selectedEndTime
        }

        val calendar = Calendar.getInstance()

        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        // Start with the event's existing time
        val timeParts = currentTime.split(":")

        if (timeParts.size == 2) {
            hour = timeParts[0].toIntOrNull() ?: hour
            minute = timeParts[1].toIntOrNull() ?: minute
        }

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                val formattedTime = String.format(
                    Locale.US,
                    "%02d:%02d",
                    selectedHour,
                    selectedMinute
                )

                if (isStartTime) {
                    selectedStartTime = formattedTime
                } else {
                    selectedEndTime = formattedTime
                }

                updateDateTimeButtons()
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun saveEventChanges() {

        if (isSaving) {
            return
        }

        val event = originalEvent ?: return

        val title =
            etEditEventTitle.text.toString().trim()

        val description =
            etEditEventDescription.text.toString().trim()

        val venue =
            etEditEventVenue.text.toString().trim()

        val address =
            etEditEventAddress.text.toString().trim()

        // Validate title
        if (title.isEmpty()) {
            etEditEventTitle.error =
                "Please enter an event title"

            etEditEventTitle.requestFocus()
            return
        }

        // Validate description
        if (description.isEmpty()) {
            etEditEventDescription.error =
                "Please enter an event description"

            etEditEventDescription.requestFocus()
            return
        }

        // Validate date and time
        if (
            selectedDate.isBlank() ||
            selectedStartTime.isBlank() ||
            selectedEndTime.isBlank()
        ) {
            Toast.makeText(
                this,
                "Please select the event date and times.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // End time must be later than start time
        if (selectedEndTime <= selectedStartTime) {
            Toast.makeText(
                this,
                "End time must be later than start time.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Validate venue
        if (venue.isEmpty()) {
            etEditEventVenue.error =
                "Please enter the venue"

            etEditEventVenue.requestFocus()
            return
        }

        // Validate address
        if (address.isEmpty()) {
            etEditEventAddress.error =
                "Please enter the address"

            etEditEventAddress.requestFocus()
            return
        }

        // Update fields while preserving ID, image,
        // and original creation timestamp
        val updatedEvent = event.copy(
            title = title,
            description = description,
            date = selectedDate,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            venue = venue,
            address = address
        )

        isSaving = true
        btnSaveEventChanges.isEnabled = false

        val savedSuccessfully =
            eventStorage.updateEvent(updatedEvent)

        if (savedSuccessfully) {

            Toast.makeText(
                this,
                "Event updated successfully!",
                Toast.LENGTH_LONG
            ).show()

            // Return to Event Details
            finish()

        } else {

            isSaving = false
            btnSaveEventChanges.isEnabled = true

            Toast.makeText(
                this,
                "Unable to update event. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showEventNotFound() {

        Toast.makeText(
            this,
            "This event could not be found.",
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(
            "selectedDate",
            selectedDate
        )

        outState.putString(
            "selectedStartTime",
            selectedStartTime
        )

        outState.putString(
            "selectedEndTime",
            selectedEndTime
        )
    }
}
