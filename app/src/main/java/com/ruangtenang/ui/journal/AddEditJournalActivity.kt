package com.ruangtenang.ui.journal

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Journal
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class AddEditJournalActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRESET_MOOD = "extra_preset_mood"
        const val EXTRA_JOURNAL_ID  = "extra_journal_id"
        // BARU: dipakai saat form dibuka dari halaman Kalender
        const val EXTRA_TARGET_DATE = "extra_target_date"
    }

    private lateinit var viewModel: JournalViewModel

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvCharCount: TextView
    private lateinit var tvSelectedMoodLabel: TextView
    private val moodButtons = mutableMapOf<String, ImageButton>()

    private val moodLabels = mapOf(
        "happy"   to "Senang",
        "calm"    to "Tenang",
        "neutral" to "Netral",
        "sad"     to "Sedih",
        "anxious" to "Cemas",
        "angry"   to "Marah",
        "shy"     to "Malu",
        "scared"  to "Takut",
        "lazy"    to "Malas"
    )

    private var selectedMood: String = "neutral"
    private var editJournalId: Int? = null
    // BARU: menyimpan tanggal yang dipilih dari Kalender (kalau ada)
    private var targetDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_journal)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        bindViews()
        setupMoodSelector()
        handleIncomingIntent()
        setupSaveButton()
        setupCharCounter()
    }

    private fun bindViews() {
        etTitle              = findViewById(R.id.et_journal_title)
        etContent            = findViewById(R.id.et_journal_content)
        tvCharCount          = findViewById(R.id.tv_char_count)
        tvSelectedMoodLabel  = findViewById(R.id.tv_selected_mood_label)

        moodButtons["happy"]   = findViewById(R.id.btn_mood_happy)
        moodButtons["calm"]    = findViewById(R.id.btn_mood_calm)
        moodButtons["neutral"] = findViewById(R.id.btn_mood_neutral)
        moodButtons["sad"]     = findViewById(R.id.btn_mood_sad)
        moodButtons["anxious"] = findViewById(R.id.btn_mood_anxious)
        moodButtons["angry"]   = findViewById(R.id.btn_mood_angry)
        moodButtons["shy"]     = findViewById(R.id.btn_mood_shy)
        moodButtons["scared"]  = findViewById(R.id.btn_mood_scared)
        moodButtons["lazy"]    = findViewById(R.id.btn_mood_lazy)

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

    private fun setupMoodSelector() {
        moodButtons.forEach { (mood, button) ->
            button.setOnClickListener {
                selectMood(mood)
            }
        }
        selectMood("neutral")
    }

    private fun handleIncomingIntent() {
        // BARU: baca tanggal titipan dari Kalender (kalau ada)
        targetDate = intent.getStringExtra(EXTRA_TARGET_DATE)

        val presetMood = intent.getStringExtra(EXTRA_PRESET_MOOD)
        if (presetMood != null) {
            selectMood(presetMood)
        }

        val journalId = intent.getIntExtra(EXTRA_JOURNAL_ID, -1)
        if (journalId != -1) {
            editJournalId = journalId
            loadExistingJournal(journalId)
        }
    }

    private fun loadExistingJournal(id: Int) {
        findViewById<TextView>(R.id.tv_toolbar_title).text = "Edit Diary"

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddEditJournalActivity)
            val journal = db.journalDao().getJournalById(id)
            journal?.let {
                runOnUiThread {
                    etTitle.setText(it.title)
                    etContent.setText(it.content)
                    selectMood(it.moodTag)
                }
            }
        }
    }

    private fun selectMood(mood: String) {
        selectedMood = mood

        moodButtons.values.forEach { btn ->
            btn.animate()
                .alpha(0.4f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start()
        }

        moodButtons[mood]?.let { btn ->
            btn.animate()
                .alpha(1.0f)
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(200)
                .start()
        }

        tvSelectedMoodLabel.text = moodLabels[mood] ?: mood.replaceFirstChar { it.uppercase() }
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

            // BARU: pakai targetDate kalau ada (dari Kalender), kalau tidak pakai hari ini
            val today = targetDate
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (editJournalId != null) {
                val updated = Journal(
                    id         = editJournalId!!,
                    title      = title,
                    content    = content,
                    moodTag    = selectedMood,
                    dateString = today,
                    timestamp  = System.currentTimeMillis()
                )
                viewModel.updateJournal(updated)
            } else {
                val newJournal = Journal(
                    title      = title,
                    content    = content,
                    moodTag    = selectedMood,
                    dateString = today,
                    timestamp  = System.currentTimeMillis()
                )
                viewModel.insertJournal(newJournal)
            }

            Toast.makeText(this, "Diary tersimpan 💙", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}