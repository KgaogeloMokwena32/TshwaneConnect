
package com.example.tshwaneconnect

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventsActivity : AppCompatActivity() {

    private lateinit var etSearchEvents: EditText

    private lateinit var btnUpcomingEvents: Button
    private lateinit var btnPastEvents: Button
    private lateinit var btnAddEvent: Button

    private lateinit var tvEventsSectionTitle: TextView
    private lateinit var eventsListContainer: LinearLayout
    private lateinit var eventsEmptyState: LinearLayout
    private lateinit var tvEmptyEventsTitle: TextView
    private lateinit var tvEmptyEventsMessage: TextView

    private lateinit var navHome: TextView
    private lateinit var navProgrammes: TextView
    private lateinit var navEvents: TextView
    private lateinit var navMore: TextView

    private lateinit var eventStorage: EventStorage

    private var selectedFilter = "upcoming"

    private val navy = Color.rgb(5, 11, 99)
    private val lightGrey = Color.rgb(228, 231, 236)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_events)

        // INITIALISE EVENT STORAGE
        eventStorage = EventStorage(this)

        // CONNECT SEARCH FIELD
        etSearchEvents = findViewById(R.id.etSearchEvents)

        // CONNECT EVENT BUTTONS
        btnUpcomingEvents = findViewById(R.id.btnUpcomingEvents)
        btnPastEvents = findViewById(R.id.btnPastEvents)
        btnAddEvent = findViewById(R.id.btnAddEvent)

        // CONNECT EVENT LIST
        tvEventsSectionTitle =
            findViewById(R.id.tvEventsSectionTitle)

        eventsListContainer =
            findViewById(R.id.eventsListContainer)

        eventsEmptyState =
            findViewById(R.id.eventsEmptyState)

        tvEmptyEventsTitle =
            findViewById(R.id.tvEmptyEventsTitle)

        tvEmptyEventsMessage =
            findViewById(R.id.tvEmptyEventsMessage)

        // CONNECT BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome)
        navProgrammes = findViewById(R.id.navProgrammes)
        navEvents = findViewById(R.id.navEvents)
        navMore = findViewById(R.id.navMore)

        // RESTORE SELECTED FILTER
        savedInstanceState?.getString("selectedFilter")?.let {
            selectedFilter = it
        }

        // UPCOMING EVENTS BUTTON
        btnUpcomingEvents.setOnClickListener {
            selectedFilter = "upcoming"
            updateEventsScreen()
        }

        // PAST EVENTS BUTTON
        btnPastEvents.setOnClickListener {
            selectedFilter = "past"
            updateEventsScreen()
        }

        // SEARCH EVENTS
        etSearchEvents.addTextChangedListener(
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
                    updateEventsScreen()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            }
        )

        // ADD NEW EVENT
        btnAddEvent.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddEventStep1Activity::class.java
                )
            )
        }

        // HOME NAVIGATION
        navHome.setOnClickListener {
            startActivity(
                Intent(this, DashboardActivity::class.java)
            )
            finish()
        }

        // PROGRAMMES NAVIGATION
        navProgrammes.setOnClickListener {
            startActivity(
                Intent(this, ProgrammesActivity::class.java)
            )
            finish()
        }

        // EVENTS - CURRENT PAGE
        navEvents.setOnClickListener {
            // Already on Events
        }

        // MORE NAVIGATION - CONNECTED
        navMore.setOnClickListener {
            startActivity(
                Intent(this, MoreActivity::class.java)
            )
            finish()
        }

        // DISPLAY EVENTS
        updateEventsScreen()
    }

    override fun onResume() {
        super.onResume()

        // REFRESH EVENTS AFTER ADDING OR EDITING
        if (::eventStorage.isInitialized) {
            updateEventsScreen()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selectedFilter", selectedFilter)
    }

    // UPDATE UPCOMING / PAST EVENTS SCREEN
    private fun updateEventsScreen() {

        val showingUpcoming = selectedFilter == "upcoming"

        // UPCOMING BUTTON COLOUR
        btnUpcomingEvents.backgroundTintList =
            ColorStateList.valueOf(
                if (showingUpcoming) navy else lightGrey
            )

        btnUpcomingEvents.setTextColor(
            if (showingUpcoming) Color.WHITE else navy
        )

        // PAST BUTTON COLOUR
        btnPastEvents.backgroundTintList =
            ColorStateList.valueOf(
                if (showingUpcoming) lightGrey else navy
            )

        btnPastEvents.setTextColor(
            if (showingUpcoming) navy else Color.WHITE
        )

        // SECTION TITLE
        tvEventsSectionTitle.text =
            if (showingUpcoming) {
                "Upcoming Events"
            } else {
                "Past Events"
            }

        // SEARCH TEXT
        val searchText =
            etSearchEvents.text.toString().trim()

        // TODAY'S DATE
        val today = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Calendar.getInstance().time)

        // LOAD SAVED EVENTS
        val savedEvents = eventStorage.getAllEvents()

        // FILTER EVENTS
        val filteredEvents = savedEvents.filter { event ->

            val matchesDate = if (showingUpcoming) {
                event.date >= today
            } else {
                event.date < today
            }

            val matchesSearch =
                event.title.contains(
                    searchText,
                    ignoreCase = true
                ) ||
                        event.description.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        event.venue.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        event.address.contains(
                            searchText,
                            ignoreCase = true
                        )

            matchesDate && matchesSearch
        }

        // CLEAR PREVIOUS EVENT CARDS
        eventsListContainer.removeAllViews()

        if (filteredEvents.isEmpty()) {

            // SHOW EMPTY STATE
            eventsEmptyState.visibility = View.VISIBLE

            tvEmptyEventsTitle.text = when {
                searchText.isNotEmpty() ->
                    "No Matching Events"

                showingUpcoming ->
                    "No Upcoming Events"

                else ->
                    "No Past Events"
            }

            tvEmptyEventsMessage.text = when {
                searchText.isNotEmpty() ->
                    "No events match \"$searchText\"."

                showingUpcoming ->
                    "Events will appear here once they have been added."

                else ->
                    "Past events will appear here when event records are available."
            }

        } else {

            // HIDE EMPTY STATE
            eventsEmptyState.visibility = View.GONE

            // DISPLAY EVENT CARDS
            filteredEvents.forEach { event ->
                addEventCard(event)
            }
        }
    }

    // CREATE EVENT CARD
    private fun addEventCard(event: TlfEvent) {

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

            contentDescription =
                "View details for ${event.title}"
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(14)
        }

        // OPTIONAL EVENT IMAGE
        if (!event.imageUri.isNullOrBlank()) {

            val imageView = ImageView(this).apply {

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(150)
                ).apply {
                    bottomMargin = dp(12)
                }

                scaleType = ImageView.ScaleType.CENTER_CROP

                try {
                    setImageURI(Uri.parse(event.imageUri))

                    if (drawable == null) {
                        visibility = View.GONE
                    }

                } catch (_: Exception) {
                    visibility = View.GONE
                }
            }

            card.addView(imageView)
        }

        // EVENT TITLE
        val titleView = TextView(this).apply {
            text = event.title
            textSize = 19f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
        }

        card.addView(titleView)

        // EVENT DATE AND TIME
        val dateView = TextView(this).apply {

            text = "${formatDate(event.date)}  |  " +
                    "${event.startTime} - ${event.endTime}"

            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(10), 0, 0)
        }

        card.addView(dateView)

        // EVENT VENUE
        val venueView = TextView(this).apply {

            text = "📍 ${event.venue}"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(8), 0, 0)
        }

        card.addView(venueView)

        // EVENT DESCRIPTION
        val descriptionView = TextView(this).apply {

            text = event.description
            textSize = 14f
            setTextColor(Color.DKGRAY)
            maxLines = 3
            setPadding(0, dp(10), 0, 0)
        }

        card.addView(descriptionView)

        // OPEN EVENT DETAILS
        card.setOnClickListener {

            val detailsIntent = Intent(
                this,
                EventDetailsActivity::class.java
            ).apply {
                putExtra("event_id", event.id)
            }

            startActivity(detailsIntent)
        }

        // ADD CARD TO SCREEN
        eventsListContainer.addView(card, cardParams)
    }

    // FORMAT EVENT DATE
    private fun formatDate(date: String): String {

        return try {

            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            inputFormat.isLenient = false

            val parsedDate = inputFormat.parse(date)

            if (parsedDate == null) {
                date
            } else {
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(parsedDate)
            }

        } catch (_: Exception) {
            date
        }
    }

    // CONVERT DP TO PIXELS
    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
