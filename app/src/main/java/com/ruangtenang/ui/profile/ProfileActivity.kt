package com.ruangtenang.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Halaman profil pengguna.
 *
 * Menampilkan statistik sederhana: total jurnal, streak, dan mood dominan.
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Tombol kembali
        findViewById<ImageButton>(R.id.btn_back_profile).setOnClickListener {
            finish()
        }

        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        val session = com.ruangtenang.data.SessionManager(this)
        val tvName = findViewById<TextView>(R.id.tv_profile_name)
        if (session.isGuestMode) {
            tvName.text = "Guest User"
        } else {
            val name = session.registeredName
            tvName.text = name ?: "Pengguna"
        }

        findViewById<View>(R.id.btn_logout).setOnClickListener {
            // Hapus status login
            session.isLoggedIn = false
            session.isGuestMode = false
            
            // Pindah ke halaman Login dan hapus tumpukan activity (history)
            val intent = android.content.Intent(this, com.ruangtenang.ui.auth.AuthActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        loadStats()
        observeMood()
    }

    private fun loadStats() {
        val db = AppDatabase.getDatabase(this)

        // Load total jurnal
        lifecycleScope.launch {
            val totalJournals = withContext(Dispatchers.IO) {
                db.journalDao().getCount()
            }
            findViewById<TextView>(R.id.tv_total_journals).text = "$totalJournals"
        }
    }



    private fun observeMood() {
        dashboardViewModel.dominantEmotion.observe(this) { (mood, _) ->
            val emoji = when (mood) {
                "happy" -> "😄"
                "sad" -> "😢"
                "anxious" -> "😰"
                "calm" -> "🍃"
                "angry" -> "😡"
                "shy" -> "😳"
                "scared" -> "😱"
                "lazy" -> "🥱"
                else -> "😐"
            }
            findViewById<TextView>(R.id.tv_dominant_mood).text = emoji
        }
    }
}
