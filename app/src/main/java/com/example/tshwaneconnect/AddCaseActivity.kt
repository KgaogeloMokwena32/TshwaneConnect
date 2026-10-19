
package com.example.tshwaneconnect

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCaseActivity : AppCompatActivity() {

    private lateinit var btnBackAddCase: TextView
    private lateinit var etCaseReference: EditText
    private lateinit var spinnerCaseProgramme: Spinner
    private lateinit var etCaseSummary: EditText
    private lateinit var spinnerCaseStatus: Spinner
    private lateinit var btnSaveCase: Button
    private lateinit var btnCancelAddCase: Button

    private lateinit var caseStorage: CaseStorage

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

        setContentView(R.layout.activity_add_case)

        caseStorage = CaseStorage(this)

        // CONNECT XML VIEWS
        btnBackAddCase = findViewById(R.id.btnBackAddCase)
        etCaseReference = findViewById(R.id.etCaseReference)
        spinnerCaseProgramme =
            findViewById(R.id.spinnerCaseProgramme)
        etCaseSummary = findViewById(R.id.etCaseSummary)
        spinnerCaseStatus =
            findViewById(R.id.spinnerCaseStatus)
        btnSaveCase = findViewById(R.id.btnSaveCase)
        btnCancelAddCase =
            findViewById(R.id.btnCancelAddCase)

        // PROGRAMME DROPDOWN
        val programmeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            programmes
        )

        programmeAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCaseProgramme.adapter = programmeAdapter

        // STATUS DROPDOWN
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statuses
        )

        statusAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCaseStatus.adapter = statusAdapter

        // BACK BUTTON
        btnBackAddCase.setOnClickListener {
            finish()
        }

        // CANCEL BUTTON
        btnCancelAddCase.setOnClickListener {
            finish()
        }

        // SAVE BUTTON
        btnSaveCase.setOnClickListener {
            saveCase()
        }
    }

    // SAVE NEW CASE
    private fun saveCase() {

        val reference = etCaseReference.text
            .toString()
            .trim()
            .uppercase()

        val summary = etCaseSummary.text
            .toString()
            .trim()

        val programme =
            spinnerCaseProgramme.selectedItem.toString()

        val status =
            spinnerCaseStatus.selectedItem.toString()

        // VALIDATE REFERENCE
        if (reference.isEmpty()) {
            etCaseReference.error =
                "Please enter a case reference."
            etCaseReference.requestFocus()
            return
        }

        // ALLOW STANDARD CASE REFERENCES
        // Example: TLF-001 or CASE-2026-001
        if (!reference.matches(Regex("[A-Z0-9-]{1,30}"))) {
            etCaseReference.error =
                "Use letters, numbers and hyphens only (maximum 30 characters)."
            etCaseReference.requestFocus()
            return
        }

        // VALIDATE PROGRAMME
        if (spinnerCaseProgramme.selectedItemPosition == 0) {
            Toast.makeText(
                this,
                "Please select a programme.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // VALIDATE SUMMARY
        if (summary.isEmpty()) {
            etCaseSummary.error =
                "Please enter a case summary."
            etCaseSummary.requestFocus()
            return
        }

        // VALIDATE STATUS
        if (spinnerCaseStatus.selectedItemPosition == 0) {
            Toast.makeText(
                this,
                "Please select a case status.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // CREATE CASE
        val newCase = TlfCase(
            reference = reference,
            programme = programme,
            summary = summary,
            status = status
        )

        btnSaveCase.isEnabled = false

        // SAVE CASE LOCALLY
        val savedSuccessfully =
            caseStorage.saveCase(newCase)

        if (savedSuccessfully) {

            Toast.makeText(
                this,
                "Case saved successfully!",
                Toast.LENGTH_LONG
            ).show()

            // RETURN TO CASES SCREEN
            finish()

        } else {

            btnSaveCase.isEnabled = true

            val duplicateReference =
                caseStorage.getAllCases().any {
                    it.reference.equals(
                        reference,
                        ignoreCase = true
                    )
                }

            val message = if (duplicateReference) {
                "This case reference already exists."
            } else {
                "Unable to save the case. Please try again."
            }

            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
