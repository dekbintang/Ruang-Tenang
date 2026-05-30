package com.ruangtenang.ui.journal

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Journal
import kotlinx.coroutines.launch

class JournalDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_JOURNAL_ID = "extra_journal_id"
    }

    private lateinit var viewModel: JournalViewModel
    private var currentJournal: Journal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_detail)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        val journalId = intent.getIntExtra(EXTRA_JOURNAL_ID, -1)
        if (journalId == -1) {
            finish()
            return
        }

        loadJournal(journalId)
        setupButtons(journalId)
    }

    private fun loadJournal(id: Int) {
        // Gunakan lifecycleScope (extension dari AppCompatActivity via lifecycle-runtime-ktx)
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@JournalDetailActivity)
            // getJournalById adalah suspend function, dipanggil di dalam coroutine — BENAR
            val journal = db.journalDao().getJournalById(id)
            journal?.let {
                currentJournal = it
                runOnUiThread { displayJournal(it) }
            }
        }
    }

    private fun displayJournal(journal: Journal) {
        val (moodLabel, moodIcon) = when (journal.moodTag) {
            "happy"   -> Pair("Senang", R.drawable.ic_mood_happy)
            "calm"    -> Pair("Tenang", R.drawable.ic_mood_calm)
            "neutral" -> Pair("Biasa",  R.drawable.ic_mood_neutral)
            "sad"     -> Pair("Sedih",  R.drawable.ic_mood_sad)
            "anxious" -> Pair("Cemas",  R.drawable.ic_mood_anxious)
            "angry"   -> Pair("Marah",  R.drawable.ic_mood_angry)
            "shy"     -> Pair("Malu",   R.drawable.ic_mood_shy)
            "scared"  -> Pair("Takut",  R.drawable.ic_mood_scared)
            "lazy"    -> Pair("Malas",  R.drawable.ic_mood_lazy)
            else      -> Pair("",       R.drawable.ic_mood_neutral)
        }
        findViewById<android.widget.ImageView>(R.id.iv_detail_mood).setImageResource(moodIcon)
        findViewById<android.widget.TextView>(R.id.tv_detail_mood_label).text = moodLabel
        findViewById<android.widget.TextView>(R.id.tv_detail_date).text = formatDate(journal.dateString)
        findViewById<android.widget.TextView>(R.id.tv_detail_title).text = journal.title
        findViewById<android.widget.TextView>(R.id.tv_detail_content).text = journal.content
    }

    private fun formatDate(dateString: String): String {
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
        } catch (e: Exception) { dateString }
    }

    private fun setupButtons(journalId: Int) {
        // Kembali
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        // Edit — buka AddEditJournalActivity dengan journal ID
        findViewById<ImageButton>(R.id.btn_edit).setOnClickListener {
            val intent = android.content.Intent(this, AddEditJournalActivity::class.java)
            intent.putExtra(AddEditJournalActivity.EXTRA_JOURNAL_ID, journalId)
            startActivity(intent)
            finish()
        }

        // Hapus
        findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Jurnal")
                .setMessage("Yakin ingin menghapus jurnal ini?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.deleteJournalById(journalId)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }
}
