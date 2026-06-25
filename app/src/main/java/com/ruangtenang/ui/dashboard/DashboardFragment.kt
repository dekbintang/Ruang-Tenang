package com.ruangtenang.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R


import com.ruangtenang.ui.journal.AddEditJournalActivity
import com.ruangtenang.ui.journal.JournalAdapter
import com.ruangtenang.ui.journal.JournalDetailActivity
import com.ruangtenang.ui.journal.JournalViewModel
import com.ruangtenang.ui.profile.ProfileActivity
import java.util.Calendar

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var journalViewModel: JournalViewModel
    private lateinit var journalAdapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel        = ViewModelProvider(this)[DashboardViewModel::class.java]
        journalViewModel = ViewModelProvider(requireActivity())[JournalViewModel::class.java]

        setupGreeting(view)

        setupObservers(view)
        setupEmojiShortcuts(view)
        setupActions(view)
        setupRecentJournals(view)

    }



    // ── Greeting dinamis ─────────────────────────────────────────────────
    private fun setupGreeting(view: View) {
        val tvGreetingEn = view.findViewById<TextView>(R.id.tv_greeting_en)
        val tvSubtitle   = view.findViewById<TextView>(R.id.tv_subtitle)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        tvGreetingEn.text = when {
            hour < 12 -> "Good Morning ☀️"
            hour < 15 -> "Good Afternoon 🌤️"
            hour < 18 -> "Good Evening 🌅"
            else      -> "Good Night 🌙"
        }

        tvSubtitle.text = listOf(
            "You are doing great today!",
            "Take a deep breath, you got this!",
            "Every small step matters 🌱",
            "Be kind to yourself today ☁️",
            "Your feelings are valid 💙"
        ).random()
    }



    // ── Emotion meter ────────────────────────────────────────────────────
    private fun setupObservers(view: View) {
        val tvProgressPercentage = view.findViewById<TextView>(R.id.tv_progress_percentage)
        val progressEmotion      = view.findViewById<android.widget.ProgressBar>(R.id.progress_emotion)
        val tvResult             = view.findViewById<TextView>(R.id.tv_emotion_result)

        viewModel.dominantEmotion.observe(viewLifecycleOwner) { (mood, percentage) ->
            tvProgressPercentage?.text     = "$percentage%"
            progressEmotion?.progress      = percentage

            val (emoji, label) = when (mood) {
                "happy"   -> "😄" to "Happy"
                "sad"     -> "😢" to "Sad"
                "anxious" -> "😰" to "Anxious"
                "calm"    -> "🍃" to "Calm"
                "angry"   -> "😡" to "Angry"
                "shy"     -> "😳" to "Shy"
                "scared"  -> "😱" to "Scared"
                "lazy"    -> "🥱" to "Lazy"
                else      -> "😐" to "Neutral"
            }
            tvResult?.text = "$emoji $label this week."
        }
    }

    // ── Mood shortcut emoji ───────────────────────────────────────────────
    private fun setupEmojiShortcuts(view: View) {
        mapOf(
            R.id.btn_emoji_happy   to "Senang",
            R.id.btn_emoji_calm    to "Tenang",
            R.id.btn_emoji_neutral to "Biasa",
            R.id.btn_emoji_sad     to "Sedih",
            R.id.btn_emoji_anxious to "Cemas",
            R.id.btn_emoji_angry   to "Marah",
            R.id.btn_emoji_shy     to "Malu",
            R.id.btn_emoji_scared  to "Takut",
            R.id.btn_emoji_lazy    to "Malas"
        ).forEach { (id, mood) ->
            view.findViewById<View>(id)?.setOnClickListener { openAddJournal(mood) }
        }
    }

    // ── Tombol aksi utama ────────────────────────────────────────────────
    private fun setupActions(view: View) {
        view.findViewById<View>(R.id.btn_profile)?.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_new_journal)?.setOnClickListener {
            openAddJournal(null)
        }
        view.findViewById<View>(R.id.btn_quick_calm)?.setOnClickListener {
            Toast.makeText(requireContext(), "🍃 Tarik nafas dalam-dalam... buang perlahan...", Toast.LENGTH_LONG).show()
        }
    }

    // ── Jurnal terakhir (horizontal) ─────────────────────────────────────
    private fun setupRecentJournals(view: View) {
        val rvJournals = view.findViewById<RecyclerView>(R.id.rv_dashboard_journals) ?: return
        val tvEmpty    = view.findViewById<TextView>(R.id.tv_empty_journals)

        journalAdapter = JournalAdapter(
            onItemClick = { journal ->
                startActivity(
                    Intent(requireContext(), JournalDetailActivity::class.java).apply {
                        putExtra(JournalDetailActivity.EXTRA_JOURNAL_ID, journal.id)
                    }
                )
            },
            onItemLongClick = { true }
        )

        rvJournals.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvJournals.adapter = journalAdapter

        journalViewModel.allJournals.observe(viewLifecycleOwner) { journals ->
            if (journals.isNullOrEmpty()) {
                rvJournals.visibility = View.GONE
                tvEmpty?.visibility   = View.VISIBLE
            } else {
                rvJournals.visibility = View.VISIBLE
                tvEmpty?.visibility   = View.GONE
                journalAdapter.submitList(journals.take(3))
            }
        }

        view.findViewById<View>(R.id.tv_see_all_journals)?.setOnClickListener {
            Toast.makeText(requireContext(), "Buka tab Jurnal untuk melihat semua.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun openAddJournal(moodKey: String?) {
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
            return
        }
        startActivity(
            Intent(requireContext(), AddEditJournalActivity::class.java).apply {
                moodKey?.let { putExtra(AddEditJournalActivity.EXTRA_PRESET_MOOD, it) }
            }
        )
    }
}