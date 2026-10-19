
package com.example.tshwaneconnect

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProgrammesActivity : AppCompatActivity() {

    private lateinit var etSearchProgrammes: EditText
    private lateinit var tvProgrammeCount: TextView
    private lateinit var programmesListContainer: LinearLayout
    private lateinit var tvEmptyProgrammes: TextView
    private lateinit var btnAddProgramme: Button

    private lateinit var navHome: TextView
    private lateinit var navProgrammes: TextView
    private lateinit var navEvents: TextView
    private lateinit var navMore: TextView

    private lateinit var programmeStorage: ProgrammeStorage

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var isAdmin = false
    private var accessVerified = false

    private val navy = Color.parseColor("#050B63")
    private val logTag = "TLF_PROGRAMMES"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_programmes)

        programmeStorage = ProgrammeStorage(this)

        etSearchProgrammes = findViewById(R.id.etSearchProgrammes)
        tvProgrammeCount = findViewById(R.id.tvProgrammeCount)
        programmesListContainer =
            findViewById(R.id.programmesListContainer)
        tvEmptyProgrammes = findViewById(R.id.tvEmptyProgrammes)
        btnAddProgramme = findViewById(R.id.btnAddProgramme)

        navHome = findViewById(R.id.navHome)
        navProgrammes = findViewById(R.id.navProgrammes)
        navEvents = findViewById(R.id.navEvents)
        navMore = findViewById(R.id.navMore)

        btnAddProgramme.isEnabled = false

        // SEARCH
        etSearchProgrammes.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    displayProgrammes(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            }
        )

        // ADD PROGRAMME
        btnAddProgramme.setOnClickListener {
            if (accessVerified) {
                showProgrammeForm(null)
            } else {
                showMessage("Please wait for account verification.")
            }
        }

        // BOTTOM NAVIGATION
        navHome.setOnClickListener {
            startActivity(
                Intent(this, DashboardActivity::class.java)
            )
            finish()
        }

        navProgrammes.setOnClickListener {
            // Already on Programmes
        }

        navEvents.setOnClickListener {
            startActivity(
                Intent(this, EventsActivity::class.java)
            )
            finish()
        }

        navMore.setOnClickListener {
            startActivity(
                Intent(this, MoreActivity::class.java)
            )
            finish()
        }

        displayProgrammes("")
    }

    override fun onResume() {
        super.onResume()
        verifyUserAccess()
    }

    // CHECK FIRESTORE ACCOUNT ROLE
    private fun verifyUserAccess() {

        accessVerified = false
        isAdmin = false
        btnAddProgramme.isEnabled = false

        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.d(logTag, "No signed-in user")
            displayProgrammes(etSearchProgrammes.text.toString())
            return
        }

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (auth.currentUser?.uid != uid) {
                    return@addOnSuccessListener
                }

                val role = document.getString("role")
                val status = document.getString("status")

                accessVerified =
                    status == "ACTIVE" &&
                            (role == "ADMIN" || role == "STAFF")

                isAdmin =
                    accessVerified && role == "ADMIN"

                Log.d(
                    logTag,
                    "Role=$role, Status=$status, " +
                            "Verified=$accessVerified, Admin=$isAdmin"
                )

                btnAddProgramme.isEnabled = accessVerified

                displayProgrammes(
                    etSearchProgrammes.text.toString()
                )
            }
            .addOnFailureListener { error ->

                accessVerified = false
                isAdmin = false
                btnAddProgramme.isEnabled = false

                Log.e(
                    logTag,
                    "Firestore permission check failed",
                    error
                )

                displayProgrammes(
                    etSearchProgrammes.text.toString()
                )

                showMessage("Unable to verify account permissions.")
            }
    }

    // DISPLAY PROGRAMMES
    private fun displayProgrammes(searchText: String) {

        programmesListContainer.removeAllViews()

        val query = searchText.trim()

        val filtered = programmeStorage
            .getAllProgrammes()
            .filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.status.contains(query, ignoreCase = true)
            }

        tvProgrammeCount.text = "Programmes (${filtered.size})"

        tvEmptyProgrammes.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE

        filtered.forEach { programme ->
            addProgrammeCard(programme)
        }
    }

    // CHECK PROGRAMME OWNERSHIP
    private fun canEdit(
        programme: ProgrammeStorage.Programme
    ): Boolean {

        val currentUid = auth.currentUser?.uid

        val allowed = accessVerified &&
                (
                        isAdmin ||
                                (
                                        !programme.isDefault &&
                                                programme.createdBy.isNotBlank() &&
                                                programme.createdBy == currentUid
                                        )
                        )

        Log.d(
            logTag,
            "Programme=${programme.name}, " +
                    "Owner=${programme.createdBy}, " +
                    "CurrentUID=$currentUid, " +
                    "Default=${programme.isDefault}, " +
                    "Verified=$accessVerified, " +
                    "Admin=$isAdmin, " +
                    "CanEdit=$allowed"
        )

        return allowed
    }

    private fun canDelete(
        programme: ProgrammeStorage.Programme
    ): Boolean {
        return !programme.isDefault && canEdit(programme)
    }

    // CREATE PROGRAMME CARD
    private fun addProgrammeCard(
        programme: ProgrammeStorage.Programme
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
            )

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }

            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(14)
        }

        val title = TextView(this).apply {
            text = programme.name.uppercase()
            textSize = 19f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
        }

        val status = TextView(this).apply {
            text = programme.status.uppercase()
            textSize = 11f
            setTypeface(null, Typeface.BOLD)

            setPadding(0, dp(8), 0, 0)

            setTextColor(
                if (programme.status.contains(
                        "Pause",
                        ignoreCase = true
                    )
                ) {
                    Color.parseColor("#A65C00")
                } else {
                    Color.parseColor("#16803C")
                }
            )
        }

        val description = TextView(this).apply {
            text = programme.description
            textSize = 14f
            setTextColor(Color.parseColor("#667085"))
            setPadding(0, dp(10), 0, 0)
        }

        val viewDetails = TextView(this).apply {
            text = "View Programme  →"
            textSize = 14f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(14), 0, 0)
        }

        card.addView(title)
        card.addView(status)
        card.addView(description)
        card.addView(viewDetails)

        card.setOnClickListener {
            showProgrammeDetails(programme)
        }

        programmesListContainer.addView(card, params)
    }

    // VIEW DETAILS AND MANAGEMENT OPTIONS
    private fun showProgrammeDetails(
        programme: ProgrammeStorage.Programme
    ) {

        val editable = canEdit(programme)
        val deletable = canDelete(programme)

        val options = mutableListOf("Close")

        if (editable) {
            options.add("Edit Programme")
        }

        if (deletable) {
            options.add("Delete Programme")
        }

        Log.d(
            logTag,
            "Visible options for ${programme.name}: $options"
        )

        AlertDialog.Builder(this)
            .setTitle(programme.name)
            .setMessage(
                "Status: ${programme.status}\n\n" +
                        programme.description
            )
            .setItems(options.toTypedArray()) { _, index ->

                when (options[index]) {

                    "Edit Programme" -> {
                        if (canEdit(programme)) {
                            showProgrammeForm(programme)
                        }
                    }

                    "Delete Programme" -> {
                        if (canDelete(programme)) {
                            confirmDeleteProgramme(programme)
                        }
                    }
                }
            }
            .show()
    }

    // ADD OR EDIT PROGRAMME
    private fun showProgrammeForm(
        existing: ProgrammeStorage.Programme?
    ) {

        if (!accessVerified) {
            showMessage("Account permissions not verified.")
            return
        }

        if (existing != null && !canEdit(existing)) {
            showMessage("You cannot edit this programme.")
            return
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(12), dp(22), 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Programme name"
            isSingleLine = true
            setText(existing?.name ?: "")
        }

        val descriptionInput = EditText(this).apply {
            hint = "Programme description"
            minLines = 3
            maxLines = 5
            gravity = Gravity.TOP
            setText(existing?.description ?: "")
        }

        val statusInput = EditText(this).apply {
            hint = "Programme status"
            isSingleLine = true
            setText(existing?.status ?: "")
        }

        container.addView(nameInput)
        container.addView(descriptionInput)
        container.addView(statusInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (existing == null)
                    "Add New Programme"
                else
                    "Edit Programme"
            )
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(
                if (existing == null)
                    "Add Programme"
                else
                    "Save Changes",
                null
            )
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = nameInput.text.toString().trim()
                val description =
                    descriptionInput.text.toString().trim()
                val status = statusInput.text.toString().trim()

                when {
                    name.isBlank() -> {
                        nameInput.error = "Enter programme name"
                    }

                    description.isBlank() -> {
                        descriptionInput.error =
                            "Enter programme description"
                    }

                    status.isBlank() -> {
                        statusInput.error = "Enter programme status"
                    }

                    !accessVerified -> {
                        showMessage("Account permissions not verified.")
                    }

                    existing != null && !canEdit(existing) -> {
                        showMessage("You cannot edit this programme.")
                    }

                    else -> {

                        val saved = if (existing == null) {

                            programmeStorage.addProgramme(
                                ProgrammeStorage.Programme(
                                    name = name,
                                    description = description,
                                    status = status
                                )
                            )

                        } else {

                            programmeStorage.updateProgramme(
                                existing.copy(
                                    name = name,
                                    description = description,
                                    status = status
                                ),
                                isAdmin
                            )
                        }

                        if (saved) {

                            showMessage(
                                if (existing == null)
                                    "Programme added successfully"
                                else
                                    "Programme updated successfully"
                            )

                            etSearchProgrammes.setText("")
                            displayProgrammes("")
                            dialog.dismiss()

                        } else {

                            showMessage(
                                "Unable to save programme. " +
                                        "Check permissions or duplicate names."
                            )
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    // DELETE CONFIRMATION
    private fun confirmDeleteProgramme(
        programme: ProgrammeStorage.Programme
    ) {

        if (!canDelete(programme)) {
            showMessage("You cannot delete this programme.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Programme")
            .setMessage(
                "Are you sure you want to delete " +
                        "\"${programme.name}\"?\n\n" +
                        "This action cannot be undone."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                if (!canDelete(programme)) {
                    showMessage("Permission denied.")
                    return@setPositiveButton
                }

                val deleted = programmeStorage.deleteProgramme(
                    programme.id,
                    isAdmin
                )

                if (deleted) {
                    showMessage("Programme deleted successfully")

                    displayProgrammes(
                        etSearchProgrammes.text.toString()
                    )
                } else {
                    showMessage("Unable to delete programme.")
                }
            }
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
