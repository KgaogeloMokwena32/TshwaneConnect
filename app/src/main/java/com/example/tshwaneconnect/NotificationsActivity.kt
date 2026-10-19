
package com.example.tshwaneconnect

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {

    private lateinit var btnBackNotifications: TextView
    private lateinit var btnNotificationsBackDashboard: Button
    private lateinit var btnRefreshNotifications: Button

    private lateinit var btnAllNotifications: Button
    private lateinit var btnCaseNotifications: Button
    private lateinit var btnEventNotifications: Button

    private lateinit var tvNotificationCount: TextView
    private lateinit var tvNotificationsSectionTitle: TextView
    private lateinit var notificationsListContainer: LinearLayout
    private lateinit var notificationsEmptyState: LinearLayout
    private lateinit var tvEmptyNotificationsMessage: TextView

    private lateinit var caseStorage: CaseStorage
    private lateinit var eventStorage: EventStorage

    private var currentFilter = "All"
    private var notificationsEnabled = true
    private var compactDisplayEnabled = false

    private data class ActivityNotification(
        val title: String,
        val message: String,
        val category: String,
        val recordId: String,
        val timestamp: Long
    )

    private var notifications =
        emptyList<ActivityNotification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notifications)

        caseStorage = CaseStorage(this)
        eventStorage = EventStorage(this)

        // CONNECT NAVIGATION
        btnBackNotifications =
            findViewById(R.id.btnBackNotifications)

        btnNotificationsBackDashboard =
            findViewById(R.id.btnNotificationsBackDashboard)

        btnRefreshNotifications =
            findViewById(R.id.btnRefreshNotifications)

        // CONNECT FILTER BUTTONS
        btnAllNotifications =
            findViewById(R.id.btnAllNotifications)

        btnCaseNotifications =
            findViewById(R.id.btnCaseNotifications)

        btnEventNotifications =
            findViewById(R.id.btnEventNotifications)

        // CONNECT NOTIFICATION VIEWS
        tvNotificationCount =
            findViewById(R.id.tvNotificationCount)

        tvNotificationsSectionTitle =
            findViewById(R.id.tvNotificationsSectionTitle)

        notificationsListContainer =
            findViewById(R.id.notificationsListContainer)

        notificationsEmptyState =
            findViewById(R.id.notificationsEmptyState)

        tvEmptyNotificationsMessage =
            findViewById(R.id.tvEmptyNotificationsMessage)

        // RESTORE SELECTED FILTER
        currentFilter =
            savedInstanceState?.getString("filter") ?: "All"

        // BACK BUTTONS
        btnBackNotifications.setOnClickListener {
            finish()
        }

        btnNotificationsBackDashboard.setOnClickListener {
            finish()
        }

        // ALL NOTIFICATIONS
        btnAllNotifications.setOnClickListener {
            currentFilter = "All"
            displayNotifications()
        }

        // CASE NOTIFICATIONS
        btnCaseNotifications.setOnClickListener {
            currentFilter = "Cases"
            displayNotifications()
        }

        // EVENT NOTIFICATIONS
        btnEventNotifications.setOnClickListener {
            currentFilter = "Events"
            displayNotifications()
        }

        // REFRESH NOTIFICATIONS
        btnRefreshNotifications.setOnClickListener {
            loadSettings()
            loadNotifications()

            Toast.makeText(
                this,
                "Notifications refreshed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        loadSettings()
        loadNotifications()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::caseStorage.isInitialized &&
            ::eventStorage.isInitialized &&
            ::notificationsListContainer.isInitialized
        ) {
            loadSettings()
            loadNotifications()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("filter", currentFilter)
        super.onSaveInstanceState(outState)
    }

    // LOAD USER SETTINGS
    private fun loadSettings() {

        val preferences = getSharedPreferences(
            SettingsActivity.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        notificationsEnabled = preferences.getBoolean(
            SettingsActivity.KEY_ACTIVITY_NOTIFICATIONS,
            true
        )

        compactDisplayEnabled = preferences.getBoolean(
            SettingsActivity.KEY_COMPACT_DISPLAY,
            false
        )
    }

    // LOAD CASE AND EVENT ACTIVITY
    private fun loadNotifications() {

        if (!notificationsEnabled) {
            notifications = emptyList()
            tvNotificationCount.text = "0"
            displayNotifications()
            return
        }

        val items = mutableListOf<ActivityNotification>()

        // CASE ACTIVITY
        caseStorage.getAllCases().forEach { savedCase ->

            items.add(
                ActivityNotification(
                    title = "Case: ${savedCase.reference}",
                    message = "${savedCase.programme} • " +
                            "Current status: ${savedCase.status}",
                    category = "Cases",
                    recordId = savedCase.id,
                    timestamp = savedCase.createdAt
                )
            )
        }

        // EVENT ACTIVITY
        eventStorage.getAllEvents().forEach { event ->

            items.add(
                ActivityNotification(
                    title = "Event: ${event.title}",
                    message = "${event.date} • ${event.venue}",
                    category = "Events",
                    recordId = event.id,
                    timestamp = event.createdAt
                )
            )
        }

        // SHOW NEWEST RECORDS FIRST
        notifications = items.sortedByDescending {
            it.timestamp
        }

        tvNotificationCount.text =
            notifications.size.toString()

        displayNotifications()
    }

    // DISPLAY NOTIFICATIONS
    private fun displayNotifications() {

        notificationsListContainer.removeAllViews()

        updateFilterButtons()

        tvNotificationsSectionTitle.text =
            when (currentFilter) {
                "Cases" -> "Case Notifications"
                "Events" -> "Event Notifications"
                else -> "All Notifications"
            }

        // NOTIFICATIONS DISABLED
        if (!notificationsEnabled) {

            notificationsEmptyState.visibility = View.VISIBLE

            tvEmptyNotificationsMessage.text =
                "Activity notifications are disabled in Settings."

            return
        }

        // APPLY FILTER
        val filteredNotifications =
            when (currentFilter) {

                "Cases" -> notifications.filter {
                    it.category == "Cases"
                }

                "Events" -> notifications.filter {
                    it.category == "Events"
                }

                else -> notifications
            }

        // EMPTY STATE
        if (filteredNotifications.isEmpty()) {

            notificationsEmptyState.visibility = View.VISIBLE

            tvEmptyNotificationsMessage.text =
                when (currentFilter) {
                    "Cases" -> "No case notifications yet"
                    "Events" -> "No event notifications yet"
                    else -> "No notifications yet"
                }

            return
        }

        notificationsEmptyState.visibility = View.GONE

        filteredNotifications.forEach { notification ->
            addNotificationCard(notification)
        }
    }

    // UPDATE FILTER BUTTON APPEARANCE
    private fun updateFilterButtons() {

        styleFilterButton(
            btnAllNotifications,
            currentFilter == "All"
        )

        styleFilterButton(
            btnCaseNotifications,
            currentFilter == "Cases"
        )

        styleFilterButton(
            btnEventNotifications,
            currentFilter == "Events"
        )
    }

    private fun styleFilterButton(
        button: Button,
        selected: Boolean
    ) {

        button.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor(
                    if (selected) "#050B63" else "#E4E8F0"
                )
            )

        button.setTextColor(
            Color.parseColor(
                if (selected) "#FFFFFF" else "#050B63"
            )
        )
    }

    // CREATE NOTIFICATION CARD
    private fun addNotificationCard(
        notification: ActivityNotification
    ) {

        val cardPadding =
            if (compactDisplayEnabled) dp(10) else dp(18)

        val card = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                cardPadding,
                cardPadding,
                cardPadding,
                cardPadding
            )

            setBackgroundColor(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(
                    if (compactDisplayEnabled) 6 else 12
                )
            }

            isClickable = true
            isFocusable = true
        }

        // CATEGORY LABEL
        val categoryText = TextView(this).apply {

            text = if (notification.category == "Cases") {
                "CASE ACTIVITY"
            } else {
                "EVENT ACTIVITY"
            }

            textSize =
                if (compactDisplayEnabled) 10f else 11f

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(Color.parseColor("#050B63"))
        }

        // NOTIFICATION TITLE
        val titleText = TextView(this).apply {

            text = notification.title

            textSize =
                if (compactDisplayEnabled) 15f else 17f

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(Color.parseColor("#050B63"))

            setPadding(
                0,
                dp(if (compactDisplayEnabled) 4 else 8),
                0,
                0
            )
        }

        // NOTIFICATION MESSAGE
        val messageText = TextView(this).apply {

            text = notification.message

            textSize =
                if (compactDisplayEnabled) 12f else 14f

            setTextColor(Color.parseColor("#64748B"))

            setPadding(0, dp(4), 0, 0)
        }

        // NOTIFICATION DATE
        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        )

        val dateText = TextView(this).apply {

            text = dateFormat.format(
                Date(notification.timestamp)
            )

            textSize =
                if (compactDisplayEnabled) 10f else 12f

            gravity = Gravity.END

            setTextColor(Color.parseColor("#94A3B8"))

            setPadding(
                0,
                dp(if (compactDisplayEnabled) 5 else 12),
                0,
                0
            )
        }

        card.addView(categoryText)
        card.addView(titleText)
        card.addView(messageText)
        card.addView(dateText)

        // OPEN RELATED CASE OR EVENT
        card.setOnClickListener {

            if (notification.category == "Cases") {

                val caseIntent = Intent(
                    this,
                    CaseDetailsActivity::class.java
                ).apply {
                    putExtra("case_id", notification.recordId)
                }

                startActivity(caseIntent)

            } else {

                val eventIntent = Intent(
                    this,
                    EventDetailsActivity::class.java
                ).apply {
                    putExtra("event_id", notification.recordId)
                }

                startActivity(eventIntent)
            }
        }

        notificationsListContainer.addView(card)
    }

    // CONVERT DP TO PIXELS
    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
