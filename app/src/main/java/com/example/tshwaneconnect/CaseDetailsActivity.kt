
package com.example.tshwaneconnect

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaseDetailsActivity : AppCompatActivity() {

    private lateinit var btnBackCaseDetails: TextView
    private lateinit var btnBackToCases: Button
    private lateinit var btnEditCase: Button
    private lateinit var btnDeleteCase: Button

    private lateinit var tvCaseDetailsReference: TextView
    private lateinit var tvCaseDetailsStatus: TextView
    private lateinit var tvCaseDetailsProgramme: TextView
    private lateinit var tvCaseDetailsCreatedAt: TextView
    private lateinit var tvCaseDetailsSummary: TextView

    private lateinit var caseStorage: CaseStorage

    private var selectedCaseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_case_details)

        caseStorage = CaseStorage(this)

        // CONNECT NAVIGATION BUTTONS
        btnBackCaseDetails =
            findViewById(R.id.btnBackCaseDetails)

        btnBackToCases =
            findViewById(R.id.btnBackToCases)

        btnEditCase =
            findViewById(R.id.btnEditCase)

        btnDeleteCase =
            findViewById(R.id.btnDeleteCase)

        // CONNECT CASE INFORMATION
        tvCaseDetailsReference =
            findViewById(R.id.tvCaseDetailsReference)

        tvCaseDetailsStatus =
            findViewById(R.id.tvCaseDetailsStatus)

        tvCaseDetailsProgramme =
            findViewById(R.id.tvCaseDetailsProgramme)

        tvCaseDetailsCreatedAt =
            findViewById(R.id.tvCaseDetailsCreatedAt)

        tvCaseDetailsSummary =
            findViewById(R.id.tvCaseDetailsSummary)

        // GET SELECTED CASE ID
        selectedCaseId = intent.getStringExtra("case_id")

        // BACK BUTTONS
        btnBackCaseDetails.setOnClickListener {
            finish()
        }

        btnBackToCases.setOnClickListener {
            finish()
        }

        // EDIT CASE
        btnEditCase.setOnClickListener {
            openEditCase()
        }

        // DELETE CASE
        btnDeleteCase.setOnClickListener {
            showDeleteConfirmation()
        }

        loadCaseDetails()
    }

    override fun onResume() {
        super.onResume()

        // REFRESH CASE INFORMATION AFTER EDITING
        if (
            ::caseStorage.isInitialized &&
            ::tvCaseDetailsReference.isInitialized
        ) {
            loadCaseDetails()
        }
    }

    // LOAD CASE DETAILS
    private fun loadCaseDetails() {

        val caseId = selectedCaseId

        if (caseId.isNullOrBlank()) {
            showCaseNotFound()
            return
        }

        val selectedCase = caseStorage.getCaseById(caseId)

        if (selectedCase == null) {
            showCaseNotFound()
            return
        }

        // DISPLAY CASE REFERENCE
        tvCaseDetailsReference.text =
            selectedCase.reference

        // DISPLAY CASE STATUS
        tvCaseDetailsStatus.text =
            selectedCase.status

        tvCaseDetailsStatus.setTextColor(
            when {
                selectedCase.status.equals(
                    "Active",
                    ignoreCase = true
                ) -> Color.rgb(21, 128, 61)

                selectedCase.status.equals(
                    "Pending",
                    ignoreCase = true
                ) -> Color.rgb(161, 98, 7)

                else -> Color.rgb(100, 116, 139)
            }
        )

        // DISPLAY PROGRAMME
        tvCaseDetailsProgramme.text =
            selectedCase.programme

        // DISPLAY SUMMARY
        tvCaseDetailsSummary.text =
            selectedCase.summary

        // FORMAT CREATION DATE
        val dateFormat = SimpleDateFormat(
            "dd MMMM yyyy, HH:mm",
            Locale.getDefault()
        )

        tvCaseDetailsCreatedAt.text =
            dateFormat.format(
                Date(selectedCase.createdAt)
            )
    }

    // OPEN EDIT CASE SCREEN
    private fun openEditCase() {

        val caseId = selectedCaseId

        if (caseId.isNullOrBlank() ||
            caseStorage.getCaseById(caseId) == null
        ) {
            showCaseNotFound()
            return
        }

        val editIntent = Intent(
            this,
            EditCaseActivity::class.java
        ).apply {
            putExtra("case_id", caseId)
        }

        startActivity(editIntent)
    }

    // SHOW DELETE CONFIRMATION
    private fun showDeleteConfirmation() {

        val caseId = selectedCaseId

        if (caseId.isNullOrBlank() ||
            caseStorage.getCaseById(caseId) == null
        ) {
            showCaseNotFound()
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Case?")
            .setMessage(
                "Are you sure you want to delete this case? " +
                        "This action cannot be undone."
            )
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                deleteSelectedCase(caseId)
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setTextColor(Color.rgb(180, 35, 24))
        }

        dialog.show()
    }

    // DELETE SELECTED CASE
    private fun deleteSelectedCase(caseId: String) {

        btnDeleteCase.isEnabled = false

        val deletedSuccessfully =
            caseStorage.deleteCase(caseId)

        if (deletedSuccessfully) {

            Toast.makeText(
                this,
                "Case deleted successfully.",
                Toast.LENGTH_LONG
            ).show()

            // RETURN TO CASES SCREEN
            finish()

        } else {

            btnDeleteCase.isEnabled = true

            Toast.makeText(
                this,
                "Unable to delete the case. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // CASE NOT FOUND
    private fun showCaseNotFound() {

        Toast.makeText(
            this,
            "This case could not be found.",
            Toast.LENGTH_LONG
        ).show()

        finish()
    }
}
