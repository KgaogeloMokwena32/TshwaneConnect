
package com.example.tshwaneconnect

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ProgrammeStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "TLF_PROGRAMMES",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PROGRAMMES_KEY = "saved_programmes"
    }

    data class Programme(
        val name: String,
        val description: String,
        val status: String,
        val id: String = UUID.randomUUID().toString(),
        val createdBy: String = "",
        val isDefault: Boolean = false
    )

    private val defaultProgrammes = listOf(
        Programme(
            name = "Akanani",
            description = "Support for homeless men and boys, including outreach, " +
                    "drop-in services and employment assistance. Some services continue.",
            status = "Project on Pause",
            id = "tlf_akanani",
            isDefault = true
        ),
        Programme(
            name = "The Potter's House",
            description = "Residential care and drop-in support for women experiencing crisis.",
            status = "Recently Reopened",
            id = "tlf_potters_house",
            isDefault = true
        ),
        Programme(
            name = "Gilead Health Unit",
            description = "Transitional residential care for people with psychosocial " +
                    "disabilities, including palliative and geriatric support.",
            status = "Operational",
            id = "tlf_gilead",
            isDefault = true
        ),
        Programme(
            name = "Isithebe",
            description = "Provides structural and strategic support for " +
                    "Tshwane Leadership Foundation's community initiatives.",
            status = "Operational",
            id = "tlf_isithebe",
            isDefault = true
        ),
        Programme(
            name = "Outreach",
            description = "Connecting with people living on the streets or in " +
                    "precarious housing situations to offer support and build relationships.",
            status = "Operational",
            id = "tlf_outreach",
            isDefault = true
        )
    )

    private fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getAllProgrammes(): List<Programme> {
        val savedJson = preferences.getString(PROGRAMMES_KEY, null)
            ?: return defaultProgrammes

        return try {
            val array = JSONArray(savedJson)
            val programmes = mutableListOf<Programme>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val name = item.getString("name")

                // Migrate original programmes saved by the old version.
                val original = defaultProgrammes.firstOrNull {
                    it.name.equals(name, ignoreCase = true)
                }

                programmes.add(
                    Programme(
                        name = name,
                        description = item.getString("description"),
                        status = item.getString("status"),
                        id = item.optString(
                            "id",
                            original?.id ?: UUID.randomUUID().toString()
                        ),
                        createdBy = item.optString("createdBy", ""),
                        isDefault = original != null ||
                                item.optBoolean("isDefault", false)
                    )
                )
            }

            programmes
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addProgramme(programme: Programme): Boolean {
        val uid = currentUserId() ?: return false

        if (programme.name.isBlank() ||
            programme.description.isBlank() ||
            programme.status.isBlank()
        ) {
            return false
        }

        val existing = getAllProgrammes()

        if (existing.any {
                it.name.equals(programme.name, ignoreCase = true)
            }
        ) {
            return false
        }

        val newProgramme = programme.copy(
            id = UUID.randomUUID().toString(),
            createdBy = uid,
            isDefault = false
        )

        return saveAllProgrammes(existing + newProgramme)
    }

    fun updateProgramme(
        updated: Programme,
        isAdmin: Boolean
    ): Boolean {
        val uid = currentUserId() ?: return false
        val existing = getAllProgrammes()
        val original = existing.find { it.id == updated.id }
            ?: return false

        if (!isAdmin && original.createdBy != uid) {
            return false
        }

        if (updated.name.isBlank() ||
            updated.description.isBlank() ||
            updated.status.isBlank()
        ) {
            return false
        }

        if (existing.any {
                it.id != updated.id &&
                        it.name.equals(updated.name, ignoreCase = true)
            }
        ) {
            return false
        }

        val result = existing.map {
            if (it.id == updated.id) {
                original.copy(
                    name = updated.name.trim(),
                    description = updated.description.trim(),
                    status = updated.status.trim()
                )
            } else {
                it
            }
        }

        return saveAllProgrammes(result)
    }

    fun deleteProgramme(
        programmeId: String,
        isAdmin: Boolean
    ): Boolean {
        val uid = currentUserId() ?: return false
        val existing = getAllProgrammes()
        val programme = existing.find { it.id == programmeId }
            ?: return false

        // The original TLF programmes cannot be deleted.
        if (programme.isDefault) {
            return false
        }

        if (!isAdmin && programme.createdBy != uid) {
            return false
        }

        return saveAllProgrammes(
            existing.filterNot { it.id == programmeId }
        )
    }

    private fun saveAllProgrammes(
        programmes: List<Programme>
    ): Boolean {
        return try {
            val array = JSONArray()

            programmes.forEach { programme ->
                array.put(
                    JSONObject().apply {
                        put("id", programme.id)
                        put("name", programme.name)
                        put("description", programme.description)
                        put("status", programme.status)
                        put("createdBy", programme.createdBy)
                        put("isDefault", programme.isDefault)
                    }
                )
            }

            preferences.edit()
                .putString(PROGRAMMES_KEY, array.toString())
                .commit()
        } catch (e: Exception) {
            false
        }
    }
}
