package com.example.tshwaneconnect

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class CaseStorage(context: Context) {

    // Keep the existing storage location so saved cases
    // are not lost when upgrading the application.
    private val preferences = context.getSharedPreferences(
        "TLF_DEMO_CASES",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val CASES_KEY = "saved_demo_cases"
    }

    // SAVE A NEW CASE
    fun saveCase(case: TlfCase): Boolean {

        // A case must have a reference number
        if (case.reference.isBlank()) {
            return false
        }

        val existingCases = getAllCases()

        // Prevent duplicate case references
        val duplicateReference = existingCases.any {
            it.reference.equals(
                case.reference,
                ignoreCase = true
            )
        }

        if (duplicateReference) {
            return false
        }

        return saveAllCases(existingCases + case)
    }

    // GET ALL SAVED CASES
    fun getAllCases(): List<TlfCase> {

        val jsonString = preferences.getString(
            CASES_KEY,
            "[]"
        ) ?: "[]"

        return try {

            val jsonArray = JSONArray(jsonString)
            val cases = mutableListOf<TlfCase>()

            for (index in 0 until jsonArray.length()) {

                val jsonObject = jsonArray.getJSONObject(index)

                val case = TlfCase(
                    id = jsonObject.getString("id"),
                    reference = jsonObject.getString("reference"),
                    programme = jsonObject.getString("programme"),
                    summary = jsonObject.getString("summary"),
                    status = jsonObject.getString("status"),
                    createdAt = jsonObject.optLong(
                        "createdAt",
                        System.currentTimeMillis()
                    )
                )

                cases.add(case)
            }

            // Show newest cases first
            cases.sortedByDescending {
                it.createdAt
            }

        } catch (_: Exception) {
            emptyList()
        }
    }

    // GET A CASE BY ID
    fun getCaseById(caseId: String): TlfCase? {

        return getAllCases().find {
            it.id == caseId
        }
    }

    // UPDATE AN EXISTING CASE
    fun updateCase(updatedCase: TlfCase): Boolean {

        if (updatedCase.reference.isBlank()) {
            return false
        }

        val existingCases = getAllCases().toMutableList()

        val index = existingCases.indexOfFirst {
            it.id == updatedCase.id
        }

        if (index == -1) {
            return false
        }

        // Prevent duplicate references
        val duplicateReference = existingCases.any {
            it.id != updatedCase.id &&
                    it.reference.equals(
                        updatedCase.reference,
                        ignoreCase = true
                    )
        }

        if (duplicateReference) {
            return false
        }

        val originalCase = existingCases[index]

        // Preserve the original creation date
        val caseToSave = updatedCase.copy(
            createdAt = originalCase.createdAt
        )

        existingCases[index] = caseToSave

        return saveAllCases(existingCases)
    }

    // DELETE A CASE
    fun deleteCase(caseId: String): Boolean {

        val existingCases = getAllCases()

        if (existingCases.none { it.id == caseId }) {
            return false
        }

        val remainingCases = existingCases.filter {
            it.id != caseId
        }

        return saveAllCases(remainingCases)
    }

    // SAVE CASES TO LOCAL STORAGE
    private fun saveAllCases(cases: List<TlfCase>): Boolean {

        return try {

            val jsonArray = JSONArray()

            cases.forEach { case ->

                val jsonObject = JSONObject().apply {
                    put("id", case.id)
                    put("reference", case.reference)
                    put("programme", case.programme)
                    put("summary", case.summary)
                    put("status", case.status)
                    put("createdAt", case.createdAt)
                }

                jsonArray.put(jsonObject)
            }

            preferences.edit()
                .putString(
                    CASES_KEY,
                    jsonArray.toString()
                )
                .commit()

        } catch (_: Exception) {
            false
        }
    }
}
