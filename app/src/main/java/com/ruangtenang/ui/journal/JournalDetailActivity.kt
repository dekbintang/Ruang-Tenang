package com.ruangtenang.ui.journal

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.model.Mood
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
        lifecycleScope.launch {
            val journal = viewModel.getJournalById(id)
            journal?.let {
                currentJournal = it
                runOnUiThread { displayJournal(it) }
            }
        }
    }

    private fun displayJournal(journal: Journal) {
        val mood = Mood.fromId(journal.moodTag)

        // Pakai emoji untuk konsistensi
        findViewById<TextView>(R.id.iv_detail_mood).text = mood.emoji
        findViewById<TextView>(R.id.tv_detail_mood_label).text = mood.label

        findViewById<TextView>(R.id.tv_detail_date).text = formatDate(journal.dateString)
        findViewById<TextView>(R.id.tv_detail_title).text = journal.title
        findViewById<TextView>(R.id.tv_detail_content).text = journal.content
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
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btn_edit).setOnClickListener {
            val intent = android.content.Intent(this, AddEditJournalActivity::class.java)
            intent.putExtra(AddEditJournalActivity.EXTRA_JOURNAL_ID, journalId)
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_hapus_diary_title)
                .setMessage(R.string.dialog_hapus_diary_message)
                .setPositiveButton(R.string.dialog_hapus_title) { _, _ ->
                    viewModel.deleteJournalById(journalId)
                    finish()
                }
                .setNegativeButton(R.string.dialog_batal, null)
                .show()
        }
    }
}