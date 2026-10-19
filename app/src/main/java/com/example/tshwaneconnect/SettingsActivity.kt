
package com.example.tshwaneconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var btnBackSettings: TextView
    private lateinit var btnSettingsBackMore: Button
    private lateinit var btnSaveSettings: Button

    private lateinit var switchActivityNotifications: CompoundButton
    private lateinit var switchCompactDisplay: CompoundButton

    companion object {
        const val PREFS_NAME = "TLF_CONNECT_SETTINGS"
        const val KEY_ACTIVITY_NOTIFICATIONS =
            "activity_notifications"
        const val KEY_COMPACT_DISPLAY = "compact_display"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        // INITIALISE FIREBASE
        auth = FirebaseAuth.getInstance()

        // CONNECT NAVIGATION BUTTONS
        btnBackSettings = findViewById(R.id.btnBackSettings)

        btnSettingsBackMore =
            findViewById(R.id.btnSettingsBackMore)

        btnSaveSettings =
            findViewById(R.id.btnSaveSettings)

        // CONNECT SWITCHES
        switchActivityNotifications =
            findViewById(R.id.switchActivityNotifications)

        switchCompactDisplay =
            findViewById(R.id.switchCompactDisplay)

        // BACK BUTTONS
        btnBackSettings.setOnClickListener {
            finish()
        }

        btnSettingsBackMore.setOnClickListener {
            finish()
        }

        // SAVE BUTTON
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    override fun onStart() {
        super.onStart()

        // CHECK LOGIN SESSION
        if (auth.currentUser == null) {
            returnToLogin()
            return
        }

        loadSettings()
    }

    // GET THE CURRENT USER'S SETTINGS FILE
    private fun getUserPreferences() =
        auth.currentUser?.uid?.let { uid ->

            getSharedPreferences(
                "${PREFS_NAME}_$uid",
                Context.MODE_PRIVATE
            )
        }

    // LOAD SAVED SETTINGS
    private fun loadSettings() {

        val preferences = getUserPreferences()

        if (preferences == null) {
            returnToLogin()
            return
        }

        val notificationsEnabled =
            preferences.getBoolean(
                KEY_ACTIVITY_NOTIFICATIONS,
                true
            )

        val compactDisplayEnabled =
            preferences.getBoolean(
                KEY_COMPACT_DISPLAY,
                false
            )

        switchActivityNotifications.isChecked =
            notificationsEnabled

        switchCompactDisplay.isChecked =
            compactDisplayEnabled
    }

    // SAVE USER SETTINGS
    private fun saveSettings() {

        val preferences = getUserPreferences()

        if (preferences == null) {
            returnToLogin()
            return
        }

        val savedSuccessfully =
            preferences.edit()
                .putBoolean(
                    KEY_ACTIVITY_NOTIFICATIONS,
                    switchActivityNotifications.isChecked
                )
                .putBoolean(
                    KEY_COMPACT_DISPLAY,
                    switchCompactDisplay.isChecked
                )
                .commit()

        if (savedSuccessfully) {

            Toast.makeText(
                this,
                "Your settings have been saved!",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Unable to save settings. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // RETURN TO LOGIN IF SESSION IS MISSING
    private fun returnToLogin() {

        val intent = Intent(
            this,
            LoginActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}
