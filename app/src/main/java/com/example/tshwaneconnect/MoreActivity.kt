package com.example.tshwaneconnect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoreActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvMoreStaffName: TextView
    private lateinit var tvMoreStaffEmail: TextView
    private lateinit var tvMoreInitials: TextView

    private lateinit var btnBackMore: TextView
    private lateinit var btnMoreBackDashboard: Button
    private lateinit var btnMoreProgrammes: Button
    private lateinit var btnMoreEvents: Button
    private lateinit var btnMoreProfile: Button
    private lateinit var btnMoreSettings: Button
    private lateinit var btnMorePrivacy: Button
    private lateinit var btnMoreLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_more)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // PROFILE INFORMATION
        tvMoreStaffName = findViewById(R.id.tvMoreStaffName)
        tvMoreStaffEmail = findViewById(R.id.tvMoreStaffEmail)
        tvMoreInitials = findViewById(R.id.tvMoreInitials)

        // NAVIGATION
        btnBackMore = findViewById(R.id.btnBackMore)
        btnMoreBackDashboard =
            findViewById(R.id.btnMoreBackDashboard)
        btnMoreProgrammes = findViewById(R.id.btnMoreProgrammes)
        btnMoreEvents = findViewById(R.id.btnMoreEvents)

        // ACCOUNT AND SETTINGS
        btnMoreProfile = findViewById(R.id.btnMoreProfile)
        btnMoreSettings = findViewById(R.id.btnMoreSettings)
        btnMorePrivacy = findViewById(R.id.btnMorePrivacy)
        btnMoreLogout = findViewById(R.id.btnMoreLogout)

        // BACK
        btnBackMore.setOnClickListener {
            finish()
        }

        // PROGRAMMES
        btnMoreProgrammes.setOnClickListener {
            startActivity(
                Intent(this, ProgrammesActivity::class.java)
            )
        }

        // EVENTS
        btnMoreEvents.setOnClickListener {
            startActivity(
                Intent(this, EventsActivity::class.java)
            )
        }

        // PROFILE
        btnMoreProfile.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }

        // SETTINGS
        btnMoreSettings.setOnClickListener {
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }

        // PRIVACY AND SECURITY
        btnMorePrivacy.setOnClickListener {
            startActivity(
                Intent(this, PrivacySecurityActivity::class.java)
            )
        }

        // LOG OUT
        btnMoreLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // DASHBOARD
        btnMoreBackDashboard.setOnClickListener {
            returnToDashboard()
        }
    }

    override fun onStart() {
        super.onStart()
        loadUserProfile()
    }

    private fun loadUserProfile() {

        val user = auth.currentUser

        if (user == null) {
            logout()
            return
        }

        tvMoreStaffName.text = "Loading profile..."
        tvMoreStaffEmail.text = user.email ?: ""
        tvMoreInitials.text = "—"

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (auth.currentUser?.uid != user.uid) {
                    return@addOnSuccessListener
                }

                val role = document.getString("role")
                val status = document.getString("status")

                if (
                    status != "ACTIVE" ||
                    role !in listOf("STAFF", "ADMIN")
                ) {
                    showMessage("Your account is not authorised.")
                    logout()
                    return@addOnSuccessListener
                }

                val fullName =
                    document.getString("fullName")
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: user.email.orEmpty()

                tvMoreStaffName.text = fullName
                tvMoreStaffEmail.text = user.email ?: ""

                // CREATE INITIALS FROM THE REAL NAME
                val words = fullName
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }

                val initials = words
                    .take(2)
                    .mapNotNull { it.firstOrNull() }
                    .joinToString("")
                    .uppercase()

                tvMoreInitials.text =
                    initials.ifEmpty { "?" }
            }
            .addOnFailureListener {
                tvMoreStaffName.text = "Profile unavailable"
                tvMoreInitials.text = "?"
                showMessage(
                    "Unable to load your profile. Check your connection."
                )
            }
    }

    private fun returnToDashboard() {

        val user = auth.currentUser

        if (user == null) {
            logout()
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (auth.currentUser?.uid != user.uid) {
                    return@addOnSuccessListener
                }

                val destination = when {
                    document.getString("role") == "ADMIN" &&
                            document.getString("status") == "ACTIVE" ->
                        AdminDashboardActivity::class.java

                    document.getString("role") == "STAFF" &&
                            document.getString("status") == "ACTIVE" ->
                        DashboardActivity::class.java

                    else -> {
                        logout()
                        return@addOnSuccessListener
                    }
                }

                val intent = Intent(this, destination).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }

                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                showMessage("Unable to verify your account.")
            }
    }

    private fun showLogoutConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Log Out?")
            .setMessage(
                "Are you sure you want to sign out of TLF Connect?"
            )
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Log Out") { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {

        auth.signOut()

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

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
