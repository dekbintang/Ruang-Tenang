package com.ruangtenang.ui.profile

import android.os.Bundle
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

        loadStats()
        observeStreak()
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

    private fun observeStreak() {
        dashboardViewModel.currentStreak.observe(this) { streak ->
            // Update angka streak di stat card
            findViewById<TextView>(R.id.tv_streak_days).text = "$streak"

            // Update warna background streak frame berdasarkan level
            val frameStreak = findViewById<android.widget.FrameLayout>(R.id.frame_streak)
            val tvStreakDays = findViewById<TextView>(R.id.tv_streak_days)

            val (bgTint, textColor) = when {
                streak >= 14 -> Pair("#FEF3C7", "#92400E") // Dark Gold
                streak >= 7  -> Pair("#FEF9C3", "#A16207") // Gold
                streak >= 4  -> Pair("#FFEDD5", "#C2410C") // Orange
                streak >= 2  -> Pair("#FEE2E2", "#DC2626") // Red
                streak >= 1  -> Pair("#FFF7ED", "#EA580C") // Soft orange
                else         -> Pair("#F1F5F9", "#64748B") // Grey
            }
            frameStreak.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(bgTint)
            )
            tvStreakDays.setTextColor(android.graphics.Color.parseColor(textColor))

            // Update streak highlight card
            val cardStreakHighlight = findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_streak_highlight)
            val tvTitle = findViewById<TextView>(R.id.tv_streak_highlight_title)
            val tvMsg = findViewById<TextView>(R.id.tv_streak_highlight_msg)
            val tvEmoji = findViewById<TextView>(R.id.tv_streak_emoji_big)

            when {
                streak >= 14 -> {
                    tvEmoji.text = "🏆"
                    tvTitle.text = "Streak $streak Hari — Legenda!"
                    tvMsg.text = "Apimu sudah berubah menjadi emas murni. Kamu luar biasa konsisten!"
                    cardStreakHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#FEF9C3"))
                }
                streak >= 7 -> {
                    tvEmoji.text = "🌟"
                    tvTitle.text = "Streak $streak Hari — Emas!"
                    tvMsg.text = "Luar biasa! Terus jaga konsistensi menulis jurnalmu."
                    cardStreakHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#FEF3C7"))
                }
                streak >= 4 -> {
                    tvEmoji.text = "🔥"
                    tvTitle.text = "Streak $streak Hari — Membara!"
                    tvMsg.text = "Apinya semakin besar. Jangan lupa kembali besok!"
                    cardStreakHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEDD5"))
                }
                streak >= 1 -> {
                    tvEmoji.text = "🔥"
                    tvTitle.text = "Streak $streak Hari"
                    tvMsg.text = "Awal yang bagus! Terus tulis jurnalmu setiap hari."
                    cardStreakHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF7ED"))
                }
                else -> {
                    tvEmoji.text = "💤"
                    tvTitle.text = "Belum Ada Streak"
                    tvMsg.text = "Tulis jurnalmu hari ini untuk menyalakan api pertama!"
                    cardStreakHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"))
                }
            }
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
