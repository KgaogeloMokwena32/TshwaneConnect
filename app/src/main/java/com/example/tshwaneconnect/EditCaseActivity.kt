
package com.example.tshwaneconnect

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditCaseActivity : AppCompatActivity() {

    private lateinit var btnBackEditCase: TextView
    private lateinit var etEditCaseReference: EditText
    private lateinit var spinnerEditCaseProgramme: Spinner
    private lateinit var etEditCaseSummary: EditText
    private lateinit var spinnerEditCaseStatus: Spinner
    private lateinit var btnSaveCaseChanges: Button
    private lateinit var btnCancelEditCase: Button

    private lateinit var caseStorage: CaseStorage

    private var originalCase: TlfCase? = null

    private val programmes = listOf(
        "Select Programme",
        "Akanani",
        "The Potter's House",
        "Gilead Health Unit",
        "Outreach",
        "Isithebe"
    )

    private val statuses = listOf(
        "Select Status",
        "Active",
        "Pending",
        "Closed"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_case)

        caseStorage = CaseStorage(this)

        // CONNECT XML VIEWS
        btnBackEditCase =
            findViewById(R.id.btnBackEditCase)

        etEditCaseReference =
            findViewById(R.id.etEditCaseReference)

        spinnerEditCaseProgramme =
            findViewById(R.id.spinnerEditCaseProgramme)

        etEditCaseSummary =
            findViewById(R.id.etEditCaseSummary)

        spinnerEditCaseStatus =
            findViewById(R.id.spinnerEditCaseStatus)

        btnSaveCaseChanges =
            findViewById(R.id.btnSaveCaseChanges)

        btnCancelEditCase =
            findViewById(R.id.btnCancelEditCase)

        // PROGRAMME DROPDOWN
        val programmeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            programmes
        )

        programmeAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerEditCaseProgramme.adapter = programmeAdapter

        // STATUS DROPDOWN
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statuses
        )

        statusAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerEditCaseStatus.adapter = statusAdapter

        // GET SELECTED CASE ID
        val caseId = intent.getStringExtra("case_id")

        if (caseId.isNullOrBlank()) {
            showCaseNotFound()
            return
        }

        // LOAD EXISTING CASE
        originalCase = caseStorage.getCaseById(caseId)

        val selectedCase = originalCase

        if (selectedCase == null) {
            showCaseNotFound()
            return
        }

        // POPULATE FORM ON FIRST CREATION
        if (savedInstanceState == null) {
            populateCaseFields(selectedCase)
        }

        // BACK BUTTON
        btnBackEditCase.setOnClickListener {
            finish()
        }

        // CANCEL BUTTON
        btnCancelEditCase.setOnClickListener {
            finish()
        }

        // SAVE CHANGES
        btnSaveCaseChanges.setOnClickListener {
            saveCaseChanges()
        }
    }

    // POPULATE FORM WITH EXISTING CASE DETAILS
    private fun populateCaseFields(case: TlfCase) {

        etEditCaseReference.setText(case.reference)

        etEditCaseSummary.setText(case.summary)

        val programmeIndex = programmes.indexOfFirst {
            it.equals(case.programme, ignoreCase = true)
        }

        spinnerEditCaseProgramme.setSelection(
            if (programmeIndex >= 0) programmeIndex else 0
        )

        val statusIndex = statuses.indexOfFirst {
            it.equals(case.status, ignoreCase = true)
        }

        spinnerEditCaseStatus.setSelection(
            if (statusIndex >= 0) statusIndex else 0
        )
    }

    // SAVE UPDATED CASE
    private fun saveCaseChanges() {

        val existingCase = originalCase

        if (existingCase == null) {
            showCaseNotFound()
            return
        }

        val reference = etEditCaseReference.text
            .toString()
            .trim()
            .uppercase()

        val summary = etEditCaseSummary.text
            .toString()
            .trim()

        val programme =
            spinnerEditCaseProgramme.selectedItem.toString()

        val status =
            spinnerEditCaseStatus.selectedItem.toString()

        // VALIDATE REFERENCE
        if (reference.isEmpty()) {
            etEditCaseReference.error =
                "Please enter a case reference."
            etEditCaseReference.requestFocus()
            return
        }

        // ACCEPT STANDARD CASE REFERENCES
        if (!reference.matches(Regex("[A-Z0-9-]{1,30}"))) {
            etEditCaseReference.error =
                "Use letters, numbers and hyphens only (maximum 30 characters)."
            etEditCaseReference.requestFocus()
            return
        }

        // VALIDATE PROGRAMME
        if (spinnerEditCaseProgramme.selectedItemPosition == 0) {
            Toast.makeText(
                this,
                "Please select a programme.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // VALIDATE SUMMARY
        if (summary.isEmpty()) {
            etEditCaseSummary.error =
                "Please enter a case summary."
            etEditCaseSummary.requestFocus()
            return
        }

        // VALIDATE STATUS
        if (spinnerEditCaseStatus.selectedItemPosition == 0) {
            Toast.makeText(
                this,
                "Please select a case status.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // CHECK FOR DUPLICATE REFERENCES
        val duplicateReference =
            caseStorage.getAllCases().any { case ->
                case.id != existingCase.id &&
                        case.reference.equals(
                            reference,
                            ignoreCase = true
                        )
            }

        if (duplicateReference) {
            etEditCaseReference.error =
                "This case reference already exists."
            etEditCaseReference.requestFocus()
            return
        }

        // PRESERVE ORIGINAL ID AND CREATION DATE
        val updatedCase = existingCase.copy(
            reference = reference,
            programme = programme,
            summary = summary,
            status = status
        )

        btnSaveCaseChanges.isEnabled = false

        // SAVE CHANGES LOCALLY
        val savedSuccessfully =
            caseStorage.updateCase(updatedCase)

        if (savedSuccessfully) {

            Toast.makeText(
                this,
                "Case updated successfully!",
                Toast.LENGTH_LONG
            ).show()

            // RETURN TO CASE DETAILS
            finish()

        } else {

            btnSaveCaseChanges.isEnabled = true

            Toast.makeText(
                this,
                "Unable to update the case. Please try again.",
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
