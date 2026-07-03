package com.ruangtenang.ui.journal

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.model.Mood
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import android.view.Gravity

class AddEditJournalActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_JOURNAL_ID  = "extra_journal_id"
        const val EXTRA_TARGET_DATE = "extra_target_date"
    }

    private lateinit var viewModel: JournalViewModel
    private lateinit var session: SessionManager

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvCharCount: TextView
    private lateinit var tvSelectedMoodLabel: TextView
    private lateinit var layoutMoodButtons: LinearLayout

    private val moodButtons = mutableMapOf<String, TextView>()

    private var selectedMood: String = "neutral"
    private var editJournalId: Int? = null
    private var targetDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_journal)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]
        session = SessionManager(this)

        bindViews()
        setupMoodButtons()           // ← tombol dibuat otomatis dari Mood.kt
        handleIncomingIntent()
        setupSaveButton()
        setupCharCounter()
    }

    private fun bindViews() {
        etTitle = findViewById(R.id.et_journal_title)
        etContent = findViewById(R.id.et_journal_content)
        tvCharCount = findViewById(R.id.tv_char_count)
        tvSelectedMoodLabel = findViewById(R.id.tv_selected_mood_label)
        layoutMoodButtons = findViewById(R.id.layout_mood_buttons)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        setupDateDisplay()
    }

    private fun setupDateDisplay() {
        val locale = Locale("id", "ID")
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        val dateStr = dateFormat.format(Date())
        val formatted = dateStr.replaceFirstChar { it.uppercase() }
        findViewById<TextView>(R.id.tv_journal_date).text = "📅 $formatted"
    }

    private fun setupMoodButtons() {
        layoutMoodButtons.removeAllViews()
        moodButtons.clear()

        val density = resources.displayMetrics.density
        val circleSize = (64 * density).toInt()   // 64dp, konsisten di semua layar
        val circleMarginEnd = (12 * density).toInt()

        Mood.MOODS.forEach { mood ->
            val btn = TextView(this).apply {
                text = mood.emoji
                textSize = 28f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.bg_mood_circle)
                layoutParams = LinearLayout.LayoutParams(circleSize, circleSize).apply {
                    marginEnd = circleMarginEnd
                }
                setOnClickListener { selectMood(mood.id) }
            }

            layoutMoodButtons.addView(btn)
            moodButtons[mood.id] = btn
        }
    }

    private fun handleIncomingIntent() {
        targetDate = intent.getStringExtra(EXTRA_TARGET_DATE)

        val journalId = intent.getIntExtra(EXTRA_JOURNAL_ID, -1)
        if (journalId != -1) {
            editJournalId = journalId
            loadExistingJournal(journalId)
        } else {
            selectMood("neutral")
        }
    }

    private fun loadExistingJournal(id: Int) {
        findViewById<TextView>(R.id.tv_toolbar_title).text = getString(R.string.title_edit_diary)

        lifecycleScope.launch {
            val journal = viewModel.getJournalById(id)
            journal?.let {
                runOnUiThread {
                    etTitle.setText(it.title)
                    etContent.setText(it.content)
                    selectMood(it.moodTag)
                }
            }
        }
    }

    private fun selectMood(moodId: String) {
        selectedMood = moodId

        moodButtons.forEach { (id, btn) ->
            val isSelected = id == moodId

            btn.setBackgroundResource(
                if (isSelected) R.drawable.bg_mood_circle_selected
                else R.drawable.bg_mood_circle
            )
        }

        val mood = Mood.fromId(moodId)
        tvSelectedMoodLabel.text = mood.label
    }

    private fun setupCharCounter() {
        etContent.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCount.text = "${s?.length ?: 0} karakter"
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.btn_save_journal).setOnClickListener {
            val title   = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Judul tidak boleh kosong"
                return@setOnClickListener
            }
            if (content.isEmpty()) {
                etContent.error = "Isi diary tidak boleh kosong"
                return@setOnClickListener
            }

            val today = targetDate
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (editJournalId != null) {
                lifecycleScope.launch {
                    val existing = viewModel.getJournalById(editJournalId!!)
                    if (existing == null) {
                        Toast.makeText(this@AddEditJournalActivity, "Diary tidak ditemukan", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }
                    val updated = existing.copy(
                        title = title,
                        content = content,
                        moodTag = selectedMood,
                        dateString = targetDate ?: existing.dateString,
                        timestamp = existing.timestamp
                    )
                    viewModel.updateJournal(updated)
                    Toast.makeText(this@AddEditJournalActivity, "Diary tersimpan 💙", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                val newJournal = Journal(
                    userId = session.getUserId(),
                    title = title,
                    content = content,
                    moodTag = selectedMood,
                    dateString = today,
                    timestamp = System.currentTimeMillis()
                )
                viewModel.insertJournal(newJournal)
                Toast.makeText(this, "Diary tersimpan 💙", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}