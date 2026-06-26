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
        // Key yang dikirim dari DashboardFragment via Intent
        const val EXTRA_PRESET_MOOD = "extra_preset_mood"
        // Key untuk mode edit (membawa ID jurnal yang mau diedit)
        const val EXTRA_JOURNAL_ID  = "extra_journal_id"
    }

    private lateinit var viewModel: JournalViewModel

    // Referensi UI
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvCharCount: TextView
    private lateinit var tvSelectedMoodLabel: TextView
    private val moodButtons = mutableMapOf<String, ImageButton>()

    // Map mood key → label teks Indonesia
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

    // State mood yang dipilih (default: neutral)
    private var selectedMood: String = "neutral"

    // Mode edit: null = buat baru, bukan null = edit jurnal existing
    private var editJournalId: Int? = null

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

        // Tombol kembali
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        // Set tanggal hari ini di header
        setupDateDisplay()
    }

    private fun setupDateDisplay() {
        val locale = Locale("id", "ID")
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        val dateStr = dateFormat.format(Date())
        // Capitalize huruf pertama
        val formatted = dateStr.replaceFirstChar { it.uppercase() }
        findViewById<TextView>(R.id.tv_journal_date).text = "📅 $formatted"
    }

    private fun setupMoodSelector() {
        moodButtons.forEach { (mood, button) ->
            button.setOnClickListener {
                selectMood(mood)
            }
        }
        // Set default visual
        selectMood("neutral")
    }

    // ── INTI Shortcut System: Terima mood dari DashboardFragment ──────────
    private fun handleIncomingIntent() {
        // Cek apakah ada preset mood dari shortcut emoji Dashboard
        val presetMood = intent.getStringExtra(EXTRA_PRESET_MOOD)
        if (presetMood != null) {
            selectMood(presetMood)
        }

        // Cek apakah ini mode edit
        val journalId = intent.getIntExtra(EXTRA_JOURNAL_ID, -1)
        if (journalId != -1) {
            editJournalId = journalId
            loadExistingJournal(journalId)
        }
    }

    private fun loadExistingJournal(id: Int) {
        // Ganti title toolbar untuk mode edit
        findViewById<TextView>(R.id.tv_toolbar_title).text = "Edit Diary"

        // Gunakan lifecycleScope (tersedia karena AppCompatActivity implements LifecycleOwner)
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddEditJournalActivity)
            // getJournalById adalah suspend function — aman dipanggil di sini
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

    // ── Pilih mood dan update tampilan visual tombol ──────────────────────
    private fun selectMood(mood: String) {
        selectedMood = mood

        // Reset semua tombol ke state non-aktif
        moodButtons.values.forEach { btn ->
            btn.animate()
                .alpha(0.4f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start()
        }

        // Highlight tombol yang dipilih dengan animasi
        moodButtons[mood]?.let { btn ->
            btn.animate()
                .alpha(1.0f)
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(200)
                .start()
        }

        // Update chip label mood yang dipilih
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

            // Validasi input
            if (title.isEmpty()) {
                etTitle.error = "Judul tidak boleh kosong"
                return@setOnClickListener
            }
            if (content.isEmpty()) {
                etContent.error = "Isi diary tidak boleh kosong"
                return@setOnClickListener
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (editJournalId != null) {
                // Mode UPDATE
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
                // Mode INSERT BARU
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
