package com.example.tshwaneconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AddEventStep3Activity : AppCompatActivity() {

    private lateinit var tvReviewEventTitle: TextView
    private lateinit var tvReviewEventDescription: TextView
    private lateinit var tvReviewEventDate: TextView
    private lateinit var tvReviewEventTime: TextView
    private lateinit var tvReviewEventVenue: TextView
    private lateinit var tvReviewEventAddress: TextView

    private lateinit var ivReviewEventImage: ImageView

    private lateinit var btnBackToStep2: TextView
    private lateinit var btnPreviousEventStep3: Button
    private lateinit var btnPublishEvent: Button

    private lateinit var eventStorage: EventStorage

    private var eventPublished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_event_step3)

        eventStorage = EventStorage(this)

        // Connect event information fields
        tvReviewEventTitle =
            findViewById(R.id.tvReviewEventTitle)

        tvReviewEventDescription =
            findViewById(R.id.tvReviewEventDescription)

        tvReviewEventDate =
            findViewById(R.id.tvReviewEventDate)

        tvReviewEventTime =
            findViewById(R.id.tvReviewEventTime)

        tvReviewEventVenue =
            findViewById(R.id.tvReviewEventVenue)

        tvReviewEventAddress =
            findViewById(R.id.tvReviewEventAddress)

        ivReviewEventImage =
            findViewById(R.id.ivReviewEventImage)

        // Connect navigation buttons
        btnBackToStep2 =
            findViewById(R.id.btnBackToStep2)

        btnPreviousEventStep3 =
            findViewById(R.id.btnPreviousEventStep3)

        btnPublishEvent =
            findViewById(R.id.btnPublishEvent)

        // Restore state if Android recreates this screen
        eventPublished = savedInstanceState?.getBoolean(
            "eventPublished",
            false
        ) ?: false

        btnPublishEvent.isEnabled = !eventPublished

        // Display event details
        displayEventInformation()

        // Return to Step 2
        btnBackToStep2.setOnClickListener {
            finish()
        }

        btnPreviousEventStep3.setOnClickListener {
            finish()
        }

        // Save event
        btnPublishEvent.setOnClickListener {
            publishEvent()
        }
    }

    private fun displayEventInformation() {

        val title =
            intent.getStringExtra("event_title").orEmpty()

        val description =
            intent.getStringExtra("event_description").orEmpty()

        val date =
            intent.getStringExtra("event_date").orEmpty()

        val startTime =
            intent.getStringExtra("event_start_time").orEmpty()

        val endTime =
            intent.getStringExtra("event_end_time").orEmpty()

        val venue =
            intent.getStringExtra("event_venue").orEmpty()

        val address =
            intent.getStringExtra("event_address").orEmpty()

        val imageUri =
            intent.getStringExtra("event_image_uri")

        tvReviewEventTitle.text =
            title.ifBlank { "Event title not provided" }

        tvReviewEventDescription.text =
            description.ifBlank {
                "Event description not provided"
            }

        tvReviewEventDate.text =
            formatEventDate(date)

        tvReviewEventTime.text =
            if (startTime.isNotBlank() && endTime.isNotBlank()) {
                "$startTime - $endTime"
            } else {
                "Time not selected"
            }

        tvReviewEventVenue.text =
            venue.ifBlank { "Venue not provided" }

        tvReviewEventAddress.text =
            address.ifBlank { "Address not provided" }

        if (!imageUri.isNullOrBlank()) {
            try {
                ivReviewEventImage.setImageURI(
                    Uri.parse(imageUri)
                )

                ivReviewEventImage.visibility =
                    View.VISIBLE

            } catch (exception: Exception) {
                ivReviewEventImage.visibility =
                    View.GONE
            }
        } else {
            ivReviewEventImage.visibility =
                View.GONE
        }
    }

    private fun publishEvent() {

        // Prevent repeated taps
        if (eventPublished) {
            return
        }

        val title =
            intent.getStringExtra("event_title").orEmpty()

        val description =
            intent.getStringExtra("event_description").orEmpty()

        val date =
            intent.getStringExtra("event_date").orEmpty()

        val startTime =
            intent.getStringExtra("event_start_time").orEmpty()

        val endTime =
            intent.getStringExtra("event_end_time").orEmpty()

        val venue =
            intent.getStringExtra("event_venue").orEmpty()

        val address =
            intent.getStringExtra("event_address").orEmpty()

        val imageUri =
            intent.getStringExtra("event_image_uri")

        // Check required information
        if (
            title.isBlank() ||
            description.isBlank() ||
            date.isBlank() ||
            startTime.isBlank() ||
            endTime.isBlank() ||
            venue.isBlank() ||
            address.isBlank()
        ) {
            Toast.makeText(
                this,
                "Some event details are missing. Please go back and check.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Create event object
        val newEvent = TlfEvent(
            title = title,
            description = description,
            date = date,
            startTime = startTime,
            endTime = endTime,
            venue = venue,
            address = address,
            imageUri = imageUri
        )

        // Save to local storage
        val savedSuccessfully =
            eventStorage.saveEvent(newEvent)

        if (savedSuccessfully) {

            eventPublished = true
            btnPublishEvent.isEnabled = false

            Toast.makeText(
                this,
                "Event saved successfully!",
                Toast.LENGTH_LONG
            ).show()

            // Return to Events and clear the creation screens
            val eventsIntent = Intent(
                this,
                EventsActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(eventsIntent)
            finish()

        } else {

            Toast.makeText(
                this,
                "Unable to save event. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun formatEventDate(date: String): String {

        if (date.isBlank()) {
            return "Date not selected"
        }

        return try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            inputFormat.isLenient = false

            val parsedDate = inputFormat.parse(date)

            val outputFormat = SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.getDefault()
            )

            if (parsedDate != null) {
                outputFormat.format(parsedDate)
            } else {
                date
            }

        } catch (exception: Exception) {
            date
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(
            "eventPublished",
            eventPublished
        )
    }
}
