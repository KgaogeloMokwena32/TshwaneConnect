package com.example.tshwaneconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var btnNotifications: TextView
    private lateinit var btnExploreProgrammes: Button
    private lateinit var btnProgrammes: Button
    private lateinit var btnEvents: Button
    private lateinit var btnCases: Button
    private lateinit var btnReports: Button
    private lateinit var btnViewEvents: Button

    private lateinit var tvUpcomingEvents: TextView
    private lateinit var eventStorage: EventStorage

    private lateinit var navHome: TextView
    private lateinit var navProgrammes: TextView
    private lateinit var navEvents: TextView
    private lateinit var navMore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        eventStorage = EventStorage(this)

        // DASHBOARD BUTTONS
        btnNotifications = findViewById(R.id.btnNotifications)
        btnExploreProgrammes = findViewById(R.id.btnExploreProgrammes)
        btnProgrammes = findViewById(R.id.btnProgrammes)
        btnEvents = findViewById(R.id.btnEvents)
        btnCases = findViewById(R.id.btnCases)
        btnReports = findViewById(R.id.btnReports)
        btnViewEvents = findViewById(R.id.btnViewEvents)

        // UPCOMING EVENTS
        tvUpcomingEvents = findViewById(R.id.tvUpcomingEvents)

        // BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome)
        navProgrammes = findViewById(R.id.navProgrammes)
        navEvents = findViewById(R.id.navEvents)
        navMore = findViewById(R.id.navMore)

        // PROGRAMMES
        btnExploreProgrammes.setOnClickListener {
            openProgrammes()
        }

        btnProgrammes.setOnClickListener {
            openProgrammes()
        }

        navProgrammes.setOnClickListener {
            openProgrammes()
        }

        // EVENTS
        btnEvents.setOnClickListener {
            openEvents()
        }

        btnViewEvents.setOnClickListener {
            openEvents()
        }

        navEvents.setOnClickListener {
            openEvents()
        }

        // CASES
        btnCases.setOnClickListener {
            openCases()
        }

        // REPORTS
        btnReports.setOnClickListener {
            openReports()
        }

        // NOTIFICATIONS
        btnNotifications.setOnClickListener {
            openNotifications()
        }

        // MORE
        navMore.setOnClickListener {
            openMore()
        }

        // HOME
        navHome.setOnClickListener {
            // Already on Dashboard
        }

        // LOAD REAL UPCOMING EVENTS
        loadUpcomingEvents()
    }

    override fun onResume() {
        super.onResume()

        // Refresh after creating, editing or deleting events
        if (
            ::eventStorage.isInitialized &&
            ::tvUpcomingEvents.isInitialized
        ) {
            loadUpcomingEvents()
        }
    }

    // DISPLAY UPCOMING EVENTS ON HOME PAGE
    private fun loadUpcomingEvents() {

        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val upcomingEvents = eventStorage.getAllEvents()
            .mapNotNull { event ->
                try {
                    val eventDate = dateFormat.parse(event.date)

                    if (eventDate != null && !eventDate.before(today)) {
                        Pair(event, eventDate)
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy { it.second.time }
            .take(3)

        if (upcomingEvents.isEmpty()) {
            tvUpcomingEvents.text =
                "No upcoming events to display yet."

            tvUpcomingEvents.setOnClickListener(null)
            return
        }

        val displayFormat = SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        )

        val eventsText = upcomingEvents.joinToString(
            separator = "\n\n"
        ) { (event, eventDate) ->

            val formattedDate = displayFormat.format(eventDate)

            "📅 ${event.title}\n" +
                    "Date: $formattedDate\n" +
                    "Time: ${event.startTime}\n" +
                    "Venue: ${event.venue}"
        }

        tvUpcomingEvents.text = eventsText

        // Open Events page when staff taps the summary
        tvUpcomingEvents.setOnClickListener {
            openEvents()
        }
    }

    private fun openProgrammes() {
        startActivity(
            Intent(this, ProgrammesActivity::class.java)
        )
    }

    private fun openEvents() {
        startActivity(
            Intent(this, EventsActivity::class.java)
        )
    }

    private fun openCases() {
        startActivity(
            Intent(this, CasesActivity::class.java)
        )
    }

    private fun openReports() {
        startActivity(
            Intent(this, ReportsActivity::class.java)
        )
    }

    private fun openNotifications() {
        startActivity(
            Intent(this, NotificationsActivity::class.java)
        )
    }

    private fun openMore() {
        startActivity(
            Intent(this, MoreActivity::class.java)
        )
    }
}
