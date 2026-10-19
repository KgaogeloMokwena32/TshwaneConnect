
package com.example.tshwaneconnect

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvTotalStaff: TextView
    private lateinit var tvPendingStaff: TextView
    private lateinit var tvAdminMessage: TextView
    private lateinit var layoutStaffRequests: LinearLayout
    private lateinit var btnAdminLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvTotalStaff = findViewById(R.id.tvTotalStaff)
        tvPendingStaff = findViewById(R.id.tvPendingStaff)
        tvAdminMessage = findViewById(R.id.tvAdminMessage)
        layoutStaffRequests = findViewById(R.id.layoutStaffRequests)
        btnAdminLogout = findViewById(R.id.btnAdminLogout)

        btnAdminLogout.setOnClickListener {
            signOut()
        }

        verifyAdministrator()
    }

    private fun verifyAdministrator() {

        val user = auth.currentUser

        if (user == null) {
            signOut()
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                val role = document.getString("role")
                val status = document.getString("status")

                if (role == "ADMIN" && status == "ACTIVE") {
                    loadStaffRequests()
                } else {
                    signOut()
                }
            }
            .addOnFailureListener {
                signOut()
            }
    }

    private fun loadStaffRequests() {

        tvAdminMessage.text = "Loading staff accounts..."

        db.collection("users")
            .whereEqualTo("role", "STAFF")
            .get()
            .addOnSuccessListener { result ->

                layoutStaffRequests.removeAllViews()

                val staff = result.documents
                val pending = staff.filter {
                    it.getString("status") == "PENDING"
                }

                tvTotalStaff.text = staff.size.toString()
                tvPendingStaff.text = pending.size.toString()

                if (pending.isEmpty()) {
                    tvAdminMessage.text =
                        "No pending staff requests."
                    return@addOnSuccessListener
                }

                tvAdminMessage.text =
                    "${pending.size} request(s) awaiting approval."

                pending.forEach { document ->

                    val name = document.getString("fullName")
                        ?: "Unknown staff member"

                    val email = document.getString("email")
                        ?: "No email"

                    addStaffCard(
                        document.id,
                        name,
                        email
                    )
                }
            }
            .addOnFailureListener {
                tvAdminMessage.text =
                    "Unable to load staff requests."
                Toast.makeText(
                    this,
                    "Check your internet connection and permissions.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun addStaffCard(
        userId: String,
        name: String,
        email: String
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 20
            layoutParams = params
        }

        val nameView = TextView(this).apply {
            text = name
            textSize = 18f
            setTextColor(Color.parseColor("#102B50"))
        }

        val emailView = TextView(this).apply {
            text = email
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        val statusView = TextView(this).apply {
            text = "Status: Pending"
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
        }

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val approveButton = Button(this).apply {
            text = "Approve"
            setOnClickListener {
                updateStaffStatus(userId, "ACTIVE")
            }
        }

        val declineButton = Button(this).apply {
            text = "Decline"
            setOnClickListener {
                updateStaffStatus(userId, "DECLINED")
            }
        }

        buttons.addView(
            approveButton,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        buttons.addView(
            declineButton,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        card.addView(nameView)
        card.addView(emailView)
        card.addView(statusView)
        card.addView(buttons)

        layoutStaffRequests.addView(card)
    }

    private fun updateStaffStatus(
        userId: String,
        newStatus: String
    ) {

        db.collection("users")
            .document(userId)
            .update("status", newStatus)
            .addOnSuccessListener {

                val message =
                    if (newStatus == "ACTIVE") {
                        "Staff account approved."
                    } else {
                        "Staff request declined."
                    }

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                loadStaffRequests()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Unable to update staff account.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun signOut() {

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
}
