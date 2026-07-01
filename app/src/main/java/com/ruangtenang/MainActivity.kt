package com.ruangtenang

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.ui.journal.AddEditJournalActivity
import com.ruangtenang.ui.journal.JournalAdapter
import com.ruangtenang.ui.journal.JournalDetailActivity
import com.ruangtenang.ui.journal.JournalViewModel
import android.widget.Button
import com.ruangtenang.data.SessionManager
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.repository.AuthRepository
import com.ruangtenang.ui.auth.LoginActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: JournalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        setupRecyclerView()
        setupSearchBar()
        setupFab()
        setupLogout()
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
    private fun setupLogout() {
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            val session = SessionManager(this)
            val userId = session.getUserId()
            val isGuest = session.isGuest()

            lifecycleScope.launch {
                if (isGuest) {
                    val db = AppDatabase.getDatabase(this@MainActivity)
                    val authRepo = AuthRepository(db.userDao())
                    db.journalDao().deleteAllByUser(userId) // hapus semua jurnal guest
                    authRepo.logoutGuest(userId) // hapus akun guest
                }
                session.clearSession()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}

