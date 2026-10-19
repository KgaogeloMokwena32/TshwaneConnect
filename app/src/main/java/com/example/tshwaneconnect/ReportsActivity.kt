
package com.example.tshwaneconnect

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private lateinit var btnBackReports: TextView
    private lateinit var btnReportsBackDashboard: Button
    private lateinit var btnRefreshReports: Button

    private lateinit var tvReportsTotalCases: TextView
    private lateinit var tvReportsActiveCases: TextView
    private lateinit var tvReportsPendingCases: TextView
    private lateinit var tvReportsClosedCases: TextView

    private lateinit var reportsProgrammeContainer: LinearLayout
    private lateinit var tvReportsEmptyProgrammes: TextView

    private lateinit var caseStorage: CaseStorage
    private lateinit var eventStorage: EventStorage

    private val navy = Color.parseColor("#050B63")
    private val lightNavy = Color.parseColor("#E8EBFA")

    private val programmeNames = listOf(
        "Akanani",
        "The Potter's House",
        "Gilead Health Unit",
        "Outreach",
        "Isithebe"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reports)

        caseStorage = CaseStorage(this)
        eventStorage = EventStorage(this)

        // NAVIGATION
        btnBackReports = findViewById(R.id.btnBackReports)

        btnReportsBackDashboard =
            findViewById(R.id.btnReportsBackDashboard)

        btnRefreshReports =
            findViewById(R.id.btnRefreshReports)

        // STATISTICS
        tvReportsTotalCases =
            findViewById(R.id.tvReportsTotalCases)

        tvReportsActiveCases =
            findViewById(R.id.tvReportsActiveCases)

        tvReportsPendingCases =
            findViewById(R.id.tvReportsPendingCases)

        tvReportsClosedCases =
            findViewById(R.id.tvReportsClosedCases)

        // PROGRAMME BREAKDOWN
        reportsProgrammeContainer =
            findViewById(R.id.reportsProgrammeContainer)

        tvReportsEmptyProgrammes =
            findViewById(R.id.tvReportsEmptyProgrammes)

        // BACK BUTTONS
        btnBackReports.setOnClickListener {
            finish()
        }

        btnReportsBackDashboard.setOnClickListener {
            finish()
        }

        // REFRESH REPORTS
        btnRefreshReports.setOnClickListener {
            loadReports()

            Toast.makeText(
                this,
                "Reports refreshed successfully.",
                Toast.LENGTH_SHORT
            ).show()
        }

        loadReports()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::caseStorage.isInitialized &&
            ::eventStorage.isInitialized &&
            ::tvReportsTotalCases.isInitialized
        ) {
            loadReports()
        }
    }

    // LOAD REPORT STATISTICS
    private fun loadReports() {

        val cases = caseStorage.getAllCases()

        val totalCases = cases.size

        val activeCases = cases.count {
            it.status.equals("Active", ignoreCase = true)
        }

        val pendingCases = cases.count {
            it.status.equals("Pending", ignoreCase = true)
        }

        val closedCases = cases.count {
            it.status.equals("Closed", ignoreCase = true)
        }

        // UPDATE STATISTICS
        tvReportsTotalCases.text = totalCases.toString()
        tvReportsActiveCases.text = activeCases.toString()
        tvReportsPendingCases.text = pendingCases.toString()
        tvReportsClosedCases.text = closedCases.toString()

        // PROGRAMME BREAKDOWN
        loadProgrammeBreakdown(cases)

        // ADDITIONAL SUMMARY
        loadAdditionalSummary(
            totalCases = totalCases,
            closedCases = closedCases
        )
    }

    // PROGRAMME STATISTICS
    private fun loadProgrammeBreakdown(cases: List<TlfCase>) {

        reportsProgrammeContainer.removeAllViews()

        if (cases.isEmpty()) {
            tvReportsEmptyProgrammes.visibility = View.VISIBLE
            return
        }

        tvReportsEmptyProgrammes.visibility = View.GONE

        programmeNames.forEach { programme ->

            val caseCount = cases.count {
                it.programme.equals(
                    programme,
                    ignoreCase = true
                )
            }

            addProgrammeRow(
                programmeName = programme,
                caseCount = caseCount,
                totalCases = cases.size
            )
        }
    }

    // CREATE PROGRAMME ROW
    private fun addProgrammeRow(
        programmeName: String,
        caseCount: Int,
        totalCases: Int
    ) {

        val percentage = if (totalCases > 0) {
            (caseCount * 100) / totalCases
        } else {
            0
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
            )

            background = roundedBackground(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
        }

        val headingRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameText = TextView(this).apply {
            text = programmeName
            textSize = 15f
            setTextColor(navy)
            typeface = Typeface.DEFAULT_BOLD

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val countText = TextView(this).apply {
            text = "$caseCount ($percentage%)"
            textSize = 14f
            setTextColor(navy)
            typeface = Typeface.DEFAULT_BOLD
        }

        headingRow.addView(nameText)
        headingRow.addView(countText)

        val progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = percentage
            progressTintList =
                android.content.res.ColorStateList.valueOf(navy)

            progressBackgroundTintList =
                android.content.res.ColorStateList.valueOf(lightNavy)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(8)
            ).apply {
                topMargin = dp(12)
            }
        }

        container.addView(headingRow)
        container.addView(progressBar)

        reportsProgrammeContainer.addView(container)
    }

    // COMPLETION RATE AND UPCOMING EVENTS
    private fun loadAdditionalSummary(
        totalCases: Int,
        closedCases: Int
    ) {

        val completionRate = if (totalCases > 0) {
            (closedCases * 100) / totalCases
        } else {
            0
        }

        val today = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())

        val upcomingEvents = eventStorage.getAllEvents().count {
            it.date >= today
        }

        // FIND OR CREATE SUMMARY CONTAINER
        val summaryId = View.generateViewId()

        // Remove previous summary to prevent duplicates
        val existingSummary =
            reportsProgrammeContainer.findViewWithTag<View>(
                "reports_summary"
            )

        if (existingSummary != null) {
            reportsProgrammeContainer.removeView(existingSummary)
        }

        val summaryContainer = LinearLayout(this).apply {
            id = summaryId
            tag = "reports_summary"
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
            )

            background = roundedBackground(
                Color.parseColor("#F0F3FF")
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(12)
            }
        }

        val heading = TextView(this).apply {
            text = "Additional Overview"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        }

        val completionText = TextView(this).apply {
            text = "Case Completion Rate: $completionRate%"
            textSize = 15f
            setTextColor(navy)
            setPadding(0, dp(12), 0, dp(8))
        }

        val completionProgress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = completionRate

            progressTintList =
                android.content.res.ColorStateList.valueOf(navy)

            progressBackgroundTintList =
                android.content.res.ColorStateList.valueOf(lightNavy)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(10)
            )
        }

        val eventsText = TextView(this).apply {
            text = "Upcoming Events: $upcomingEvents"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
            setPadding(0, dp(16), 0, 0)
        }

        summaryContainer.addView(heading)
        summaryContainer.addView(completionText)
        summaryContainer.addView(completionProgress)
        summaryContainer.addView(eventsText)

        reportsProgrammeContainer.addView(summaryContainer)
    }

    // ROUNDED CARD BACKGROUND
    private fun roundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(12).toFloat()
        }
    }

    // CONVERT DP TO PIXELS
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
