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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.model.Mood
import com.ruangtenang.ui.journal.AddEditJournalActivity
import com.ruangtenang.ui.journal.JournalDetailActivity
import com.ruangtenang.ui.journal.JournalViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var tvMonthYear: TextView
    private lateinit var gridCalendar: GridLayout
    private lateinit var tvSelectedDateHeader: TextView
    private lateinit var layoutEntriesContainer: LinearLayout
    private lateinit var layoutEntryEmpty: LinearLayout
    private lateinit var tvEntryEmptyMessage: TextView
    private lateinit var btnAddForDate: Button

    private lateinit var viewModel: JournalViewModel

    private val calendar = Calendar.getInstance()
    private var selectedDate: String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var journalDatesInMonth: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

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
        tvEntryEmptyMessage = findViewById(R.id.tv_entry_empty_message)
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
            val items = viewModel.getMoodsForMonth(monthPattern)
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
            val isFuture = dateString > todayString

            val cell = TextView(this).apply {
                text = day.toString()
                gravity = Gravity.CENTER
                textSize = 14f
                setPadding(0, 20, 0, 20)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    val margin = (2 * resources.displayMetrics.density).toInt()
                    setMargins(margin, margin, margin, margin)
                }

                val bgColor = when {
                    isSelected -> ContextCompat.getColor(this@CalendarActivity, R.color.calendar_selected)
                    isToday -> ContextCompat.getColor(this@CalendarActivity, R.color.calendar_today_bg)
                    hasEntry -> ContextCompat.getColor(this@CalendarActivity, R.color.calendar_has_entry_bg)
                    else -> Color.TRANSPARENT
                }
                setBackground(android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(bgColor)
                })

                setTextColor(
                    when {
                        isSelected -> Color.WHITE
                        isToday -> ContextCompat.getColor(this@CalendarActivity, R.color.calendar_today_text)
                        isFuture -> ContextCompat.getColor(this@CalendarActivity, R.color.calendar_future_text)
                        else -> ContextCompat.getColor(this@CalendarActivity, R.color.calendar_default_text)
                    }
                )

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

        val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isToday = selectedDate == todayString
        val isFuture = selectedDate > todayString

        lifecycleScope.launch {
            val journals = viewModel.getJournalsByDate(selectedDate)

            if (journals.isNotEmpty()) {
                layoutEntriesContainer.visibility = View.VISIBLE
                layoutEntryEmpty.visibility = View.GONE
                renderEntries(journals)
            } else {
                layoutEntriesContainer.visibility = View.GONE
                layoutEntryEmpty.visibility = View.VISIBLE

                tvEntryEmptyMessage.text = when {
                    isFuture -> "Tanggal ini belum terjadi, belum ada diary."
                    isToday -> "Belum ada diary untuk hari ini."
                    else -> "Tidak ada diary yang tercatat pada tanggal ini."
                }

                // Menambah diary hanya diperbolehkan untuk hari ini,
                // supaya diary tetap mencerminkan tanggal kejadian sebenarnya.
                if (isToday) {
                    btnAddForDate.visibility = View.VISIBLE
                    btnAddForDate.setOnClickListener {
                        val intent = Intent(this@CalendarActivity, AddEditJournalActivity::class.java)
                        intent.putExtra(AddEditJournalActivity.EXTRA_TARGET_DATE, selectedDate)
                        startActivity(intent)
                    }
                } else {
                    btnAddForDate.visibility = View.GONE
                }
            }
        }
    }

    private fun renderEntries(journals: List<Journal>) {
        layoutEntriesContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        journals.forEach { journal ->
            val cardView = inflater.inflate(R.layout.item_journal, layoutEntriesContainer, false)

            cardView.findViewById<TextView>(R.id.tv_journal_title).text = journal.title
            cardView.findViewById<TextView>(R.id.tv_journal_preview).text = journal.content
            cardView.findViewById<TextView>(R.id.tv_journal_date).text = formatDisplayDate(journal.dateString)

            // Pakai emoji dari Mood.kt (konsisten dengan statistik)
            val mood = Mood.fromId(journal.moodTag)
            cardView.findViewById<TextView>(R.id.iv_journal_mood).text = mood.emoji

            cardView.setOnClickListener {
                val intent = Intent(this, JournalDetailActivity::class.java)
                intent.putExtra(JournalDetailActivity.EXTRA_JOURNAL_ID, journal.id)
                startActivity(intent)
            }

            layoutEntriesContainer.addView(cardView)
        }
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