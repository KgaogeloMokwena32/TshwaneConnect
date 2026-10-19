package com.example.tshwaneconnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEventStep1Activity : AppCompatActivity() {

    private lateinit var etEventTitle: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var etEventDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText

    private lateinit var btnBackToEvents: TextView
    private lateinit var btnCancelEvent: Button
    private lateinit var btnNextEventStep: Button

    private val selectedDate = Calendar.getInstance()
    private var dateHasBeenSelected = false
    private var startMinutes: Int? = null
    private var endMinutes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_event_step1)

        // Connect XML fields
        etEventTitle = findViewById(R.id.etEventTitle)
        etEventDescription = findViewById(R.id.etEventDescription)
        etEventDate = findViewById(R.id.etEventDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)

        // Connect buttons
        btnBackToEvents = findViewById(R.id.btnBackToEvents)
        btnCancelEvent = findViewById(R.id.btnCancelEvent)
        btnNextEventStep = findViewById(R.id.btnNextEventStep)

        // Restore selected date and time after recreation
        if (savedInstanceState != null) {
            dateHasBeenSelected =
                savedInstanceState.getBoolean("dateSelected")

            if (dateHasBeenSelected) {
                selectedDate.timeInMillis =
                    savedInstanceState.getLong("selectedDate")
            }

            if (savedInstanceState.containsKey("startMinutes")) {
                startMinutes =
                    savedInstanceState.getInt("startMinutes")
            }

            if (savedInstanceState.containsKey("endMinutes")) {
                endMinutes =
                    savedInstanceState.getInt("endMinutes")
            }
        }

        // Open date picker
        etEventDate.setOnClickListener {
            showDatePicker()
        }

        // Open start time picker
        etStartTime.setOnClickListener {
            showTimePicker(true)
        }

        // Open end time picker
        etEndTime.setOnClickListener {
            showTimePicker(false)
        }

        // Cancel event creation
        btnBackToEvents.setOnClickListener {
            finish()
        }

        btnCancelEvent.setOnClickListener {
            finish()
        }

        // Move to Step 2
        btnNextEventStep.setOnClickListener {
            validateAndOpenStepTwo()
        }
    }

    private fun showDatePicker() {

        val calendar = if (dateHasBeenSelected) {
            selectedDate
        } else {
            Calendar.getInstance()
        }

        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->

                selectedDate.set(year, month, day)
                dateHasBeenSelected = true

                val formatter = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                )

                etEventDate.setText(
                    formatter.format(selectedDate.time)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Only allow today or future dates
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        dialog.datePicker.minDate = today.timeInMillis

        dialog.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {

        val now = Calendar.getInstance()

        val savedMinutes = if (isStartTime) {
            startMinutes
        } else {
            endMinutes
        }

        val initialHour =
            savedMinutes?.div(60)
                ?: now.get(Calendar.HOUR_OF_DAY)

        val initialMinute =
            savedMinutes?.rem(60)
                ?: now.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, hour, minute ->

                val totalMinutes = hour * 60 + minute

                val formattedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    hour,
                    minute
                )

                if (isStartTime) {
                    startMinutes = totalMinutes
                    etStartTime.setText(formattedTime)
                } else {
                    endMinutes = totalMinutes
                    etEndTime.setText(formattedTime)
                }
            },
            initialHour,
            initialMinute,
            true
        ).show()
    }

    private fun validateAndOpenStepTwo() {

        val title =
            etEventTitle.text.toString().trim()

        val description =
            etEventDescription.text.toString().trim()

        if (title.isEmpty()) {
            etEventTitle.error = "Enter an event title"
            etEventTitle.requestFocus()
            return
        }

        if (description.isEmpty()) {
            etEventDescription.error =
                "Enter an event description"

            etEventDescription.requestFocus()
            return
        }

        if (!dateHasBeenSelected) {
            showMessage("Please select an event date")
            return
        }

        if (startMinutes == null) {
            showMessage("Please select a start time")
            return
        }

        if (endMinutes == null) {
            showMessage("Please select an end time")
            return
        }

        if (endMinutes!! <= startMinutes!!) {
            showMessage(
                "End time must be later than start time"
            )
            return
        }

        // Prepare information for Step 2
        val intent = Intent(
            this,
            AddEventStep2Activity::class.java
        ).apply {

            putExtra("event_title", title)

            putExtra(
                "event_description",
                description
            )

            putExtra(
                "event_date",
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(selectedDate.time)
            )

            putExtra(
                "event_start_time",
                etStartTime.text.toString()
            )

            putExtra(
                "event_end_time",
                etEndTime.text.toString()
            )
        }

        // Open Step 2
        startActivity(intent)
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(
            "dateSelected",
            dateHasBeenSelected
        )

        if (dateHasBeenSelected) {
            outState.putLong(
                "selectedDate",
                selectedDate.timeInMillis
            )
        }

        startMinutes?.let {
            outState.putInt("startMinutes", it)
        }

        endMinutes?.let {
            outState.putInt("endMinutes", it)
        }
    }
}
