package com.ruangtenang

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.model.Mood
import com.ruangtenang.ui.journal.AddEditJournalActivity
import com.ruangtenang.ui.journal.JournalAdapter
import com.ruangtenang.ui.journal.JournalDetailActivity
import com.ruangtenang.ui.journal.JournalViewModel
import android.widget.Button
import com.ruangtenang.ui.profile.ProfileActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: JournalAdapter
    private val moodChips = mutableMapOf<String?, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        setupRecyclerView()
        setupSearchBar()
        setupMoodFilterChips()
        setupFab()
        setupProfileButton()
    }

    private fun setupRecyclerView() {
        adapter = JournalAdapter(
            onItemClick = { journal ->
                val intent = Intent(this, JournalDetailActivity::class.java)
                intent.putExtra(JournalDetailActivity.EXTRA_JOURNAL_ID, journal.id)
                startActivity(intent)
            },
            onItemLongClick = { journal ->
                showDeleteDialog(journal)
                true
            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.rv_journals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Observasi hasil pencarian
        viewModel.searchResults.observe(this) { journals ->
            adapter.submitList(journals)
            val isEmpty = journals.isEmpty()
            findViewById<View>(R.id.layout_empty_state).visibility =
                if (isEmpty) View.VISIBLE else View.GONE
            findViewById<View>(R.id.rv_journals).visibility =
                if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun setupSearchBar() {
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupMoodFilterChips() {
        val container = findViewById<LinearLayout>(R.id.layout_mood_filter)
        val density = resources.displayMetrics.density
        val paddingH = (16 * density).toInt()
        val paddingV = (8 * density).toInt()
        val chipMarginEnd = (8 * density).toInt()

        fun makeChip(key: String?, label: String): TextView {
            return TextView(this).apply {
                text = label
                textSize = 13f
                setPadding(paddingH, paddingV, paddingH, paddingV)
                setBackgroundResource(R.drawable.bg_chip_filter)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.textPrimary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = chipMarginEnd }
                gravity = Gravity.CENTER
                setOnClickListener {
                    viewModel.setMoodFilter(key)
                }
            }
        }

        // Chip "Semua" selalu ada di paling depan
        val allChip = makeChip(null, "Semua")
        container.addView(allChip)
        moodChips[null] = allChip

        // Chip per mood, emoji + label biar gampang dikenali
        Mood.MOODS.forEach { mood ->
            val chip = makeChip(mood.id, "${mood.emoji} ${mood.label}")
            container.addView(chip)
            moodChips[mood.id] = chip
        }

        viewModel.selectedMood.observe(this) { selected ->
            moodChips.forEach { (key, chip) ->
                val isSelected = key == selected
                chip.setBackgroundResource(
                    if (isSelected) R.drawable.bg_chip_filter_selected else R.drawable.bg_chip_filter
                )
                chip.setTextColor(
                    if (isSelected) ContextCompat.getColor(this, android.R.color.white)
                    else ContextCompat.getColor(this, R.color.textPrimary)
                )
            }
        }
    }

    private fun setupFab() {
        findViewById<Button>(R.id.btn_new_journal).setOnClickListener {
            startActivity(Intent(this, AddEditJournalActivity::class.java))
        }
        findViewById<Button>(R.id.btn_open_calendar).setOnClickListener {
            startActivity(Intent(this, com.ruangtenang.ui.calendar.CalendarActivity::class.java))
        }
    }

    private fun showDeleteDialog(journal: Journal) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Diary")
            .setMessage("Yakin ingin menghapus \"${journal.title}\"? Tindakan ini tidak bisa dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteJournal(journal)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupProfileButton() {
        findViewById<Button>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}