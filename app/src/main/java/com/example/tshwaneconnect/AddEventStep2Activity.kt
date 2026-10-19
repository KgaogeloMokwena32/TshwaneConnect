package com.example.tshwaneconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AddEventStep2Activity : AppCompatActivity() {

    private lateinit var etEventVenue: EditText
    private lateinit var etEventAddress: EditText
    private lateinit var ivEventPreview: ImageView
    private lateinit var tvSelectedImageName: TextView

    private lateinit var btnChooseEventImage: Button
    private lateinit var btnPreviousEventStep: Button
    private lateinit var btnNextEventStep2: Button
    private lateinit var btnBackToStep1: TextView

    private var selectedImageUri: Uri? = null

    // Open the system document picker.
    // This allows us to keep permission to read the image.
    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        if (uri != null) {

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers may not support persistent access.
            }

            selectedImageUri = uri

            ivEventPreview.setImageURI(uri)
            tvSelectedImageName.text = "Event image selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_event_step2)

        // Connect XML fields
        etEventVenue = findViewById(R.id.etEventVenue)
        etEventAddress = findViewById(R.id.etEventAddress)
        ivEventPreview = findViewById(R.id.ivEventPreview)

        tvSelectedImageName =
            findViewById(R.id.tvSelectedImageName)

        // Connect buttons
        btnChooseEventImage =
            findViewById(R.id.btnChooseEventImage)

        btnPreviousEventStep =
            findViewById(R.id.btnPreviousEventStep)

        btnNextEventStep2 =
            findViewById(R.id.btnNextEventStep2)

        btnBackToStep1 =
            findViewById(R.id.btnBackToStep1)

        // Restore information if the screen is recreated
        if (savedInstanceState != null) {

            etEventVenue.setText(
                savedInstanceState.getString("venue", "")
            )

            etEventAddress.setText(
                savedInstanceState.getString("address", "")
            )

            selectedImageUri =
                savedInstanceState.getString("imageUri")
                    ?.let { Uri.parse(it) }

            selectedImageUri?.let { uri ->
                ivEventPreview.setImageURI(uri)
                tvSelectedImageName.text =
                    "Event image selected"
            }
        }

        // Choose event image
        btnChooseEventImage.setOnClickListener {
            imagePicker.launch(arrayOf("image/*"))
        }

        // Return to Step 1
        btnPreviousEventStep.setOnClickListener {
            finish()
        }

        btnBackToStep1.setOnClickListener {
            finish()
        }

        // Validate and open Step 3
        btnNextEventStep2.setOnClickListener {
            validateAndOpenStepThree()
        }
    }

    private fun validateAndOpenStepThree() {

        val venue = etEventVenue.text.toString().trim()
        val address = etEventAddress.text.toString().trim()

        if (venue.isEmpty()) {
            etEventVenue.error =
                "Please enter the event venue"
            etEventVenue.requestFocus()
            return
        }

        if (address.isEmpty()) {
            etEventAddress.error =
                "Please enter the event address"
            etEventAddress.requestFocus()
            return
        }

        // Create the Intent for Step 3
        val stepThreeIntent = Intent(
            this,
            AddEventStep3Activity::class.java
        ).apply {

            // Information from Step 1
            putExtra(
                "event_title",
                intent.getStringExtra("event_title")
            )

            putExtra(
                "event_description",
                intent.getStringExtra("event_description")
            )

            putExtra(
                "event_date",
                intent.getStringExtra("event_date")
            )

            putExtra(
                "event_start_time",
                intent.getStringExtra("event_start_time")
            )

            putExtra(
                "event_end_time",
                intent.getStringExtra("event_end_time")
            )

            // Information from Step 2
            putExtra("event_venue", venue)
            putExtra("event_address", address)

            selectedImageUri?.let { uri ->
                putExtra(
                    "event_image_uri",
                    uri.toString()
                )
            }
        }

        // Open Review and Publish screen
        startActivity(stepThreeIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(
            "venue",
            etEventVenue.text.toString()
        )

        outState.putString(
            "address",
            etEventAddress.text.toString()
        )

        outState.putString(
            "imageUri",
            selectedImageUri?.toString()
        )
    }
}
