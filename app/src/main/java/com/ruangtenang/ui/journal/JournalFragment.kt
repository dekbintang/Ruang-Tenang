package com.ruangtenang.ui.journal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.ruangtenang.R
import com.ruangtenang.data.entity.Journal

class JournalFragment : Fragment() {

    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        setupRecyclerView(view)
        setupSearchBar(view)
        setupFab(view)
    }

    private fun setupRecyclerView(view: View) {
        adapter = JournalAdapter(
            onItemClick = { journal ->
                // Buka halaman detail jurnal
                val intent = Intent(requireContext(), JournalDetailActivity::class.java)
                intent.putExtra(JournalDetailActivity.EXTRA_JOURNAL_ID, journal.id)
                startActivity(intent)
            },
            onItemLongClick = { journal ->
                showDeleteDialog(journal)
                true
            }
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_journals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observasi hasil pencarian (reaktif)
        viewModel.searchResults.observe(viewLifecycleOwner) { journals ->
            adapter.submitList(journals)
            val isEmpty = journals.isEmpty()
            // Tampilkan empty state dan sembunyikan RecyclerView jika list kosong
            view.findViewById<View>(R.id.layout_empty_state).visibility =
                if (isEmpty) View.VISIBLE else View.GONE
            view.findViewById<View>(R.id.rv_journals).visibility =
                if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun setupSearchBar(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupFab(view: View) {
        view.findViewById<ExtendedFloatingActionButton>(R.id.fab_new_journal).setOnClickListener {
            val session = com.ruangtenang.data.SessionManager(requireContext())
            if (!session.isLoggedIn) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Login Required")
                    .setMessage("Fitur Jurnal hanya tersedia untuk pengguna yang sudah login. Silakan login terlebih dahulu.")
                    .setPositiveButton("Login") { _, _ ->
                        startActivity(Intent(requireContext(), com.ruangtenang.ui.auth.AuthActivity::class.java))
                    }
                    .setNegativeButton("Batal", null)
                    .show()
                return@setOnClickListener
            }
            startActivity(Intent(requireContext(), AddEditJournalActivity::class.java))
        }
    }

    private fun showDeleteDialog(journal: Journal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jurnal")
            .setMessage("Yakin ingin menghapus \"${journal.title}\"? Tindakan ini tidak bisa dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteJournal(journal)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
