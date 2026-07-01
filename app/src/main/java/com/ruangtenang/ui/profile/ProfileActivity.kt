package com.ruangtenang.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.db.MoodCount
import com.ruangtenang.data.repository.AuthRepository
import com.ruangtenang.data.repository.JournalRepository
import com.ruangtenang.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var journalRepository: JournalRepository

    private lateinit var ivPhoto: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etAge: EditText
    private lateinit var layoutMoodChart: LinearLayout

    private var currentPhotoUri: String? = null

    private val moodOrder = listOf(
        "neutral", "calm", "happy", "shy", "lazy",
        "anxious", "angry", "sad", "scared"
    )
    private val moodEmoji = mapOf(
        "happy" to "😄", "calm" to "🙂", "neutral" to "😐",
        "sad" to "😢", "anxious" to "😟", "angry" to "😠",
        "shy" to "☺️", "scared" to "😨", "lazy" to "😴"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* ignore kalau tidak didukung sumbernya */ }
            currentPhotoUri = it.toString()
            ivPhoto.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        session = SessionManager(this)
        val db = AppDatabase.getDatabase(this)
        authRepository = AuthRepository(db.userDao())
        journalRepository = JournalRepository(db.journalDao())

        bindViews()
        loadProfile()
        loadMoodStats()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_change_photo).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        findViewById<Button>(R.id.btn_save_profile).setOnClickListener { saveProfile() }
        findViewById<Button>(R.id.btn_logout).setOnClickListener { doLogout() }
    }

    private fun bindViews() {
        ivPhoto = findViewById(R.id.iv_profile_photo)
        etUsername = findViewById(R.id.et_profile_username)
        etAge = findViewById(R.id.et_profile_age)
        layoutMoodChart = findViewById(R.id.layout_mood_chart)
    }

    private fun loadProfile() {
        val userId = session.getUserId()
        lifecycleScope.launch {
            val user = authRepository.getUserById(userId)
            user?.let {
                etUsername.setText(it.username)
                etAge.setText(it.age?.toString() ?: "")
                currentPhotoUri = it.photoUri
                if (!it.photoUri.isNullOrEmpty()) {
                    try {
                        ivPhoto.setImageURI(Uri.parse(it.photoUri))
                    } catch (e: Exception) { }
                }
            }
        }
    }

    private fun saveProfile() {
        val userId = session.getUserId()
        val ageText = etAge.text.toString().trim()
        val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null

        lifecycleScope.launch {
            authRepository.updateProfile(userId, age, currentPhotoUri)
            Toast.makeText(this@ProfileActivity, "Profil disimpan 💙", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMoodStats() {
        val userId = session.getUserId()
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        lifecycleScope.launch {
            val stats = journalRepository.getMoodStats(userId, thirtyDaysAgo)
            renderChart(stats)
        }
    }

    private fun renderChart(stats: List<MoodCount>) {
        layoutMoodChart.removeAllViews()
        val countMap = stats.associateBy({ it.mood_tag }, { it.count })
        val maxCount = (countMap.values.maxOrNull() ?: 1).coerceAtLeast(1)
        val maxBarHeightDp = 130

        moodOrder.forEach { mood ->
            val count = countMap[mood] ?: 0

            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val barHeightDp = if (count == 0) 0 else (count.toFloat() / maxCount * maxBarHeightDp).toInt().coerceAtLeast(8)
            val barHeightPx = (barHeightDp * resources.displayMetrics.density).toInt()

            val bar = View(this).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#3B82F6"))
                layoutParams = LinearLayout.LayoutParams(
                    (24 * resources.displayMetrics.density).toInt(),
                    barHeightPx
                )
            }

            val emoji = TextView(this).apply {
                text = moodEmoji[mood] ?: "❓"
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }

            barContainer.addView(bar)
            barContainer.addView(emoji)
            layoutMoodChart.addView(barContainer)
        }
    }

    private fun doLogout() {
        val userId = session.getUserId()
        val isGuest = session.isGuest()

        lifecycleScope.launch {
            if (isGuest) {
                val db = AppDatabase.getDatabase(this@ProfileActivity)
                db.journalDao().deleteAllByUser(userId)
                authRepository.logoutGuest(userId)
            }
            session.clearSession()
            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            finish()
        }
    }
}