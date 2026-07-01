package com.ruangtenang.ui.calendar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.ui.journal.AddEditJournalActivity
import com.ruangtenang.ui.journal.JournalDetailActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var tvMonthYear: TextView
    private lateinit var gridCalendar: GridLayout
    private lateinit var tvSelectedDateHeader: TextView
    private lateinit var layoutEntriesContainer: LinearLayout
    private lateinit var layoutEntryEmpty: LinearLayout
    private lateinit var btnAddForDate: Button

    private val calendar = Calendar.getInstance()
    private var selectedDate: String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var journalDatesInMonth: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        bindViews()
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btn_prev_month).setOnClickListener { changeMonth(-1) }
        findViewById<ImageButton>(R.id.btn_next_month).setOnClickListener { changeMonth(1) }

        loadMonth()
    }

    override fun onResume() {
        super.onResume()
        loadMonth()
    }

    private fun bindViews() {
        tvMonthYear = findViewById(R.id.tv_month_year)
        gridCalendar = findViewById(R.id.grid_calendar)
        tvSelectedDateHeader = findViewById(R.id.tv_selected_date_header)
        layoutEntriesContainer = findViewById(R.id.layout_entries_container)
        layoutEntryEmpty = findViewById(R.id.layout_entry_empty)
        btnAddForDate = findViewById(R.id.btn_add_for_date)
    }

    private fun changeMonth(offset: Int) {
        calendar.add(Calendar.MONTH, offset)
        loadMonth()
    }

    private fun loadMonth() {
        val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvMonthYear.text = monthLabelFormat.format(calendar.time).uppercase()

        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val monthPattern = "$monthKey-%"

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@CalendarActivity)
            val items = db.journalDao().getMoodsForMonth(monthPattern)
            journalDatesInMonth = items.map { it.date_string }.toSet()
            buildGrid(monthKey)
            showSelectedDate()
        }
    }

    private fun buildGrid(monthKey: String) {
        gridCalendar.removeAllViews()

        val displayCal = calendar.clone() as Calendar
        displayCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        for (i in 1 until firstDayOfWeek) {
            gridCalendar.addView(makeEmptyCell())
        }

        for (day in 1..daysInMonth) {
            val dateString = "$monthKey-${day.toString().padStart(2, '0')}"
            val isToday = dateString == todayString
            val isSelected = dateString == selectedDate
            val hasEntry = journalDatesInMonth.contains(dateString)

            val cell = TextView(this).apply {
                text = day.toString()
                gravity = Gravity.CENTER
                textSize = 14f
                setPadding(0, 24, 0, 24)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                when {
                    isSelected -> {
                        setBackgroundColor(Color.parseColor("#1565A8"))
                        setTextColor(Color.WHITE)
                    }
                    isToday -> {
                        setBackgroundColor(Color.parseColor("#BBDEFB"))
                        setTextColor(Color.parseColor("#1565A8"))
                    }
                    hasEntry -> {
                        setBackgroundColor(Color.parseColor("#E3F2FD"))
                        setTextColor(Color.parseColor("#333333"))
                    }
                    else -> {
                        setBackgroundColor(Color.TRANSPARENT)
                        setTextColor(Color.parseColor("#333333"))
                    }
                }

                setOnClickListener {
                    selectedDate = dateString
                    buildGrid(monthKey)
                    showSelectedDate()
                }
            }
            gridCalendar.addView(cell)
        }
    }

    private fun makeEmptyCell(): View {
        val cell = TextView(this)
        cell.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        return cell
    }

    private fun showSelectedDate() {
        tvSelectedDateHeader.text = formatDisplayDate(selectedDate)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@CalendarActivity)
            val journals = db.journalDao().getJournalsByDate(selectedDate)

            if (journals.isNotEmpty()) {
                layoutEntriesContainer.visibility = View.VISIBLE
                layoutEntryEmpty.visibility = View.GONE
                renderEntries(journals)
            } else {
                layoutEntriesContainer.visibility = View.GONE
                layoutEntryEmpty.visibility = View.VISIBLE
                btnAddForDate.setOnClickListener {
                    val intent = Intent(this@CalendarActivity, AddEditJournalActivity::class.java)
                    intent.putExtra(AddEditJournalActivity.EXTRA_TARGET_DATE, selectedDate)
                    startActivity(intent)
                }
            }
        }
    }

    // Menampilkan satu card per diary, pakai layout item_journal.xml yang sudah ada
    private fun renderEntries(journals: List<Journal>) {
        layoutEntriesContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        journals.forEach { journal ->
            val cardView = inflater.inflate(R.layout.item_journal, layoutEntriesContainer, false)

            cardView.findViewById<TextView>(R.id.tv_journal_title).text = journal.title
            cardView.findViewById<TextView>(R.id.tv_journal_preview).text = journal.content
            cardView.findViewById<TextView>(R.id.tv_journal_date).text = formatDisplayDate(journal.dateString)
            cardView.findViewById<ImageView>(R.id.iv_journal_mood).setImageResource(getMoodIcon(journal.moodTag))

            cardView.setOnClickListener {
                val intent = Intent(this, JournalDetailActivity::class.java)
                intent.putExtra(JournalDetailActivity.EXTRA_JOURNAL_ID, journal.id)
                startActivity(intent)
            }

            layoutEntriesContainer.addView(cardView)
        }
    }

    private fun getMoodIcon(moodTag: String): Int = when (moodTag) {
        "happy"   -> R.drawable.ic_mood_happy
        "calm"    -> R.drawable.ic_mood_calm
        "neutral" -> R.drawable.ic_mood_neutral
        "sad"     -> R.drawable.ic_mood_sad
        "anxious" -> R.drawable.ic_mood_anxious
        "angry"   -> R.drawable.ic_mood_angry
        "shy"     -> R.drawable.ic_mood_shy
        "scared"  -> R.drawable.ic_mood_scared
        "lazy"    -> R.drawable.ic_mood_lazy
        else      -> R.drawable.ic_mood_neutral
    }

    private fun formatDisplayDate(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"
                "04" -> "Apr"; "05" -> "Mei"; "06" -> "Jun"
                "07" -> "Jul"; "08" -> "Agu"; "09" -> "Sep"
                "10" -> "Okt"; "11" -> "Nov"; "12" -> "Des"
                else -> parts[1]
            }
            val day = parts[2].trimStart('0')
            "$day $month $year"
        } catch (e: Exception) {
            dateString
        }
    }
}