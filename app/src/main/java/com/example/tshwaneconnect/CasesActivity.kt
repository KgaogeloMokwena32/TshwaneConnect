package com.example.tshwaneconnect

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CasesActivity : AppCompatActivity() {

    private lateinit var btnBackCases: TextView
    private lateinit var tvTotalCases: TextView
    private lateinit var tvActiveCases: TextView
    private lateinit var etSearchCases: EditText

    private lateinit var btnAllCases: Button
    private lateinit var btnActiveCases: Button
    private lateinit var btnPendingCases: Button
    private lateinit var btnClosedCases: Button

    private lateinit var tvCasesSectionTitle: TextView
    private lateinit var casesListContainer: LinearLayout
    private lateinit var casesEmptyState: LinearLayout
    private lateinit var tvEmptyCasesMessage: TextView
    private lateinit var btnAddCase: Button

    private lateinit var caseStorage: CaseStorage

    private val navy = Color.rgb(5, 11, 99)
    private val lightGrey = Color.rgb(228, 232, 240)

    private var selectedFilter = "All"

    private var allCases: List<TlfCase> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cases)

        caseStorage = CaseStorage(this)

        // CONNECT XML VIEWS
        btnBackCases = findViewById(R.id.btnBackCases)
        tvTotalCases = findViewById(R.id.tvTotalCases)
        tvActiveCases = findViewById(R.id.tvActiveCases)
        etSearchCases = findViewById(R.id.etSearchCases)

        btnAllCases = findViewById(R.id.btnAllCases)
        btnActiveCases = findViewById(R.id.btnActiveCases)
        btnPendingCases = findViewById(R.id.btnPendingCases)
        btnClosedCases = findViewById(R.id.btnClosedCases)

        tvCasesSectionTitle =
            findViewById(R.id.tvCasesSectionTitle)

        casesListContainer =
            findViewById(R.id.casesListContainer)

        casesEmptyState =
            findViewById(R.id.casesEmptyState)

        tvEmptyCasesMessage =
            findViewById(R.id.tvEmptyCasesMessage)

        btnAddCase = findViewById(R.id.btnAddCase)

        // RESTORE SELECTED FILTER
        selectedFilter = savedInstanceState?.getString(
            "selectedFilter",
            "All"
        ) ?: "All"

        // BACK BUTTON
        btnBackCases.setOnClickListener {
            finish()
        }

        // OPEN ADD CASE SCREEN
        btnAddCase.setOnClickListener {
            val intent = Intent(
                this,
                AddCaseActivity::class.java
            )
            startActivity(intent)
        }

        // STATUS FILTERS
        btnAllCases.setOnClickListener {
            selectedFilter = "All"
            updateCasesScreen()
        }

        btnActiveCases.setOnClickListener {
            selectedFilter = "Active"
            updateCasesScreen()
        }

        btnPendingCases.setOnClickListener {
            selectedFilter = "Pending"
            updateCasesScreen()
        }

        btnClosedCases.setOnClickListener {
            selectedFilter = "Closed"
            updateCasesScreen()
        }

        // SEARCH CASES
        etSearchCases.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    updateCasesScreen()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            }
        )

        loadCases()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::caseStorage.isInitialized &&
            ::casesListContainer.isInitialized
        ) {
            loadCases()
        }
    }

    // LOAD SAVED CASES
    private fun loadCases() {
        allCases = caseStorage.getAllCases()
        updateCasesScreen()
    }

    // UPDATE CASE STATISTICS AND LIST
    private fun updateCasesScreen() {

        // TOTAL CASES
        tvTotalCases.text = allCases.size.toString()

        // ACTIVE CASES
        tvActiveCases.text = allCases.count {
            it.status.equals(
                "Active",
                ignoreCase = true
            )
        }.toString()

        // UPDATE FILTER BUTTON COLOURS
        val buttons = mapOf(
            "All" to btnAllCases,
            "Active" to btnActiveCases,
            "Pending" to btnPendingCases,
            "Closed" to btnClosedCases
        )

        buttons.forEach { (status, button) ->

            val isSelected = selectedFilter == status

            button.backgroundTintList =
                ColorStateList.valueOf(
                    if (isSelected) navy else lightGrey
                )

            button.setTextColor(
                if (isSelected) Color.WHITE else navy
            )
        }

        // UPDATE SECTION TITLE
        tvCasesSectionTitle.text =
            if (selectedFilter == "All") {
                "All Cases"
            } else {
                "$selectedFilter Cases"
            }

        val searchText =
            etSearchCases.text.toString().trim()

        // FILTER CASES BY STATUS AND SEARCH TEXT
        val filteredCases = allCases.filter { case ->

            val matchesStatus =
                selectedFilter == "All" ||
                        case.status.equals(
                            selectedFilter,
                            ignoreCase = true
                        )

            val matchesSearch =
                case.reference.contains(
                    searchText,
                    ignoreCase = true
                ) ||
                        case.programme.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        case.summary.contains(
                            searchText,
                            ignoreCase = true
                        )

            matchesStatus && matchesSearch
        }

        // CLEAR PREVIOUS CASE CARDS
        casesListContainer.removeAllViews()

        if (filteredCases.isEmpty()) {

            casesEmptyState.visibility = View.VISIBLE

            tvEmptyCasesMessage.text =
                if (searchText.isNotEmpty()) {
                    "No cases match your search."
                } else if (selectedFilter != "All") {
                    "No ${selectedFilter.lowercase()} cases found."
                } else {
                    "No cases have been recorded yet."
                }

        } else {

            casesEmptyState.visibility = View.GONE

            filteredCases.forEach { case ->
                addCaseCard(case)
            }
        }
    }

    // CREATE A CASE CARD
    private fun addCaseCard(case: TlfCase) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )

            setBackgroundColor(Color.WHITE)
            elevation = dp(2).toFloat()

            isClickable = true
            isFocusable = true
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        }

        // CASE REFERENCE
        val referenceView = TextView(this).apply {
            text = case.reference
            textSize = 18f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
        }

        card.addView(referenceView)

        // PROGRAMME
        val programmeView = TextView(this).apply {
            text = "Programme: ${case.programme}"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(10), 0, 0)
        }

        card.addView(programmeView)

        // CASE SUMMARY
        val summaryView = TextView(this).apply {
            text = case.summary
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(8), 0, 0)
        }

        card.addView(summaryView)

        // CASE STATUS
        val statusView = TextView(this).apply {
            text = "Status: ${case.status}"
            textSize = 14f

            setTextColor(
                when {
                    case.status.equals(
                        "Active",
                        ignoreCase = true
                    ) -> Color.rgb(21, 128, 61)

                    case.status.equals(
                        "Pending",
                        ignoreCase = true
                    ) -> Color.rgb(161, 98, 7)

                    else -> Color.rgb(100, 116, 139)
                }
            )

            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
        }

        card.addView(statusView)

        // OPEN CASE DETAILS
        card.setOnClickListener {

            val intent = Intent(
                this,
                CaseDetailsActivity::class.java
            ).apply {
                putExtra("case_id", case.id)
            }

            startActivity(intent)
        }

        // ADD CARD TO SCREEN
        casesListContainer.addView(
            card,
            cardParams
        )
    }

    // CONVERT DP TO PIXELS
    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(
            "selectedFilter",
            selectedFilter
        )
    }
}
