package com.example.tshwaneconnect

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PrivacySecurityActivity : AppCompatActivity() {

    private lateinit var btnBackPrivacy: TextView
    private lateinit var btnPrivacyBackMore: Button
    private lateinit var btnSavePrivacyAcknowledgement: Button
    private lateinit var checkPrivacyAcknowledgement: CheckBox

    companion object {
        private const val PREFS_NAME = "TLF_PRIVACY_SETTINGS"
        private const val KEY_ACKNOWLEDGED = "privacy_acknowledged"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_privacy_security)

        // CONNECT SCREEN ELEMENTS
        btnBackPrivacy = findViewById(R.id.btnBackPrivacy)

        btnPrivacyBackMore =
            findViewById(R.id.btnPrivacyBackMore)

        btnSavePrivacyAcknowledgement =
            findViewById(R.id.btnSavePrivacyAcknowledgement)

        checkPrivacyAcknowledgement =
            findViewById(R.id.checkPrivacyAcknowledgement)

        // LOAD PREVIOUS ACKNOWLEDGEMENT
        loadAcknowledgement()

        // TOP BACK BUTTON
        btnBackPrivacy.setOnClickListener {
            finish()
        }

        // BACK TO MORE BUTTON
        btnPrivacyBackMore.setOnClickListener {
            finish()
        }

        // SAVE ACKNOWLEDGEMENT
        btnSavePrivacyAcknowledgement.setOnClickListener {
            saveAcknowledgement()
        }
    }

    private fun loadAcknowledgement() {

        val preferences = getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val acknowledged = preferences.getBoolean(
            KEY_ACKNOWLEDGED,
            false
        )

        checkPrivacyAcknowledgement.isChecked = acknowledged
    }

    private fun saveAcknowledgement() {

        if (!checkPrivacyAcknowledgement.isChecked) {

            Toast.makeText(
                this,
                "Please tick the acknowledgement checkbox first.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val preferences = getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val savedSuccessfully = preferences.edit()
            .putBoolean(KEY_ACKNOWLEDGED, true)
            .commit()

        if (savedSuccessfully) {

            Toast.makeText(
                this,
                "Acknowledgement saved successfully!",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Unable to save acknowledgement. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
