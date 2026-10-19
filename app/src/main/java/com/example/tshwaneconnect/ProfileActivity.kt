
package com.example.tshwaneconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // NAVIGATION BUTTONS
    private lateinit var btnBackProfile: TextView
    private lateinit var btnProfileSettings: Button
    private lateinit var btnProfilePrivacy: Button
    private lateinit var btnProfileBackMore: Button

    // PROFILE INFORMATION
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileRole: TextView
    private lateinit var tvProfileStatus: TextView
    private lateinit var tvProfileFullName: TextView
    private lateinit var tvProfileEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        // INITIALISE FIREBASE
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // CONNECT NAVIGATION BUTTONS
        btnBackProfile = findViewById(R.id.btnBackProfile)

        btnProfileSettings =
            findViewById(R.id.btnProfileSettings)

        btnProfilePrivacy =
            findViewById(R.id.btnProfilePrivacy)

        btnProfileBackMore =
            findViewById(R.id.btnProfileBackMore)

        // CONNECT PROFILE TEXT VIEWS
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileRole = findViewById(R.id.tvProfileRole)
        tvProfileStatus = findViewById(R.id.tvProfileStatus)

        tvProfileFullName =
            findViewById(R.id.tvProfileFullName)

        tvProfileEmail = findViewById(R.id.tvProfileEmail)

        // TOP BACK BUTTON
        btnBackProfile.setOnClickListener {
            finish()
        }

        // OPEN SETTINGS
        btnProfileSettings.setOnClickListener {
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }

        // OPEN PRIVACY AND SECURITY
        btnProfilePrivacy.setOnClickListener {
            startActivity(
                Intent(this, PrivacySecurityActivity::class.java)
            )
        }

        // RETURN TO MORE
        btnProfileBackMore.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        loadProfile()
    }

    // LOAD THE LOGGED-IN USER'S REAL PROFILE
    private fun loadProfile() {

        val currentUser = auth.currentUser

        // CHECK IF SOMEONE IS LOGGED IN
        if (currentUser == null) {
            returnToLogin()
            return
        }

        // SHOW LOADING INFORMATION
        tvProfileName.text = "Loading profile..."
        tvProfileFullName.text = "Loading..."
        tvProfileEmail.text = currentUser.email ?: ""
        tvProfileRole.text = "Loading..."
        tvProfileStatus.text = "Loading..."

        // GET USER INFORMATION FROM FIRESTORE
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->

                // ENSURE THE SAME USER IS STILL LOGGED IN
                if (auth.currentUser?.uid != currentUser.uid) {
                    return@addOnSuccessListener
                }

                if (!document.exists()) {
                    showMessage("User profile not found.")
                    auth.signOut()
                    returnToLogin()
                    return@addOnSuccessListener
                }

                val fullName =
                    document.getString("fullName")
                        ?.trim()
                        .orEmpty()

                val role = document.getString("role")
                val status = document.getString("status")

                // ONLY APPROVED STAFF AND ADMINS CAN ACCESS
                if (
                    status != "ACTIVE" ||
                    role !in listOf("STAFF", "ADMIN")
                ) {
                    showMessage("Your account is not authorised.")
                    auth.signOut()
                    returnToLogin()
                    return@addOnSuccessListener
                }

                // DISPLAY REAL USER NAME
                val displayName = fullName.ifBlank {
                    "Name unavailable"
                }

                tvProfileName.text = displayName
                tvProfileFullName.text = displayName

                // DISPLAY EMAIL FROM FIREBASE AUTHENTICATION
                tvProfileEmail.text =
                    currentUser.email
                        ?: document.getString("email")
                                ?: "Email unavailable"

                // DISPLAY USER ROLE
                tvProfileRole.text = when (role) {
                    "ADMIN" -> "Administrator"
                    "STAFF" -> "Staff Member"
                    else -> "Unknown"
                }

                // DISPLAY ACCOUNT STATUS
                tvProfileStatus.text = when (status) {
                    "ACTIVE" -> "ACTIVE"
                    "PENDING" -> "PENDING APPROVAL"
                    "DECLINED" -> "DECLINED"
                    else -> "UNKNOWN"
                }
            }
            .addOnFailureListener {

                tvProfileName.text = "Profile unavailable"
                tvProfileFullName.text = "Unavailable"
                tvProfileRole.text = "Unavailable"
                tvProfileStatus.text = "Unavailable"

                showMessage(
                    "Unable to load profile. Check your internet connection."
                )
            }
    }

    // RETURN TO LOGIN SCREEN
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

    // DISPLAY MESSAGE
    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
