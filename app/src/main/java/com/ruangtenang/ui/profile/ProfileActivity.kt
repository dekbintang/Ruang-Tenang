package com.ruangtenang.ui.profile

import android.text.InputFilter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.db.MoodCount
import com.ruangtenang.data.model.Mood
import com.ruangtenang.data.repository.AuthRepository
import com.ruangtenang.data.repository.JournalRepository
import com.ruangtenang.ui.auth.LoginActivity
import com.ruangtenang.ui.journal.JournalViewModel
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
        private const val STATS_WINDOW_DAYS = 30L
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
        private const val MAX_BAR_HEIGHT_DP = 130
        private const val MIN_AGE = 1
        private const val MAX_AGE = 120

        // Hanya izinkan huruf, angka, dan simbol ASCII cetak (tanpa spasi & tanpa emoticon/emoji)
        private val passwordInputFilter = InputFilter { source, start, end, _, _, _ ->
            val filtered = source.subSequence(start, end).filter { it.code in 33..126 }
            if (filtered.length == end - start) null else filtered
        }
    }

    private lateinit var session: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var journalRepository: JournalRepository
    private lateinit var viewModel: JournalViewModel

    private lateinit var ivPhoto: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etAge: EditText
    private lateinit var tvEmail: TextView
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var layoutMoodChart: LinearLayout

    private var currentPhotoUri: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengambil persistable URI permission untuk foto profil", e)
            }
            currentPhotoUri = it.toString()
            applyRealPhoto(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        session = SessionManager(this)
        val db = AppDatabase.getDatabase(this)
        authRepository = AuthRepository(db.userDao())
        journalRepository = JournalRepository(db.journalDao())
        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        bindViews()
        loadProfile()
        loadMoodStats()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btn_change_photo).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        findViewById<Button>(R.id.btn_save_profile).setOnClickListener { saveProfile() }
        findViewById<Button>(R.id.btn_change_password).setOnClickListener { changePassword() }
        findViewById<Button>(R.id.btn_logout).setOnClickListener { doLogout() }
    }

    private fun bindViews() {
        ivPhoto = findViewById(R.id.iv_profile_photo)
        etUsername = findViewById(R.id.et_profile_username)
        etAge = findViewById(R.id.et_profile_age)
        tvEmail = findViewById(R.id.tv_profile_email)
        etOldPassword = findViewById(R.id.et_old_password)
        etNewPassword = findViewById(R.id.et_new_password)
        layoutMoodChart = findViewById(R.id.layout_mood_chart)

        etOldPassword.filters = etOldPassword.filters + passwordInputFilter
        etNewPassword.filters = etNewPassword.filters + passwordInputFilter
    }

    private fun loadProfile() {
        val userId = session.getUserId()
        lifecycleScope.launch {
            val user = authRepository.getUserById(userId)
            user?.let {
                etUsername.setText(it.username)
                etAge.setText(it.age?.toString() ?: "")
                tvEmail.text = if (it.isGuest) "Akun tamu (tanpa email)" else it.email
                currentPhotoUri = it.photoUri
                if (!it.photoUri.isNullOrEmpty()) {
                    try {
                        applyRealPhoto(Uri.parse(it.photoUri))
                    } catch (e: Exception) {
                        Log.e(TAG, "Gagal memuat foto profil dari URI: ${it.photoUri}", e)
                    }
                }
            }
        }
    }

    private fun applyRealPhoto(uri: Uri) {
        ivPhoto.setPadding(0, 0, 0, 0)
        ivPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
        ivPhoto.imageTintList = null
        ivPhoto.setImageURI(uri)
    }

    private fun changePassword() {
        val userId = session.getUserId()
        val oldPassword = etOldPassword.text.toString()
        val newPassword = etNewPassword.text.toString()

        if (oldPassword.isEmpty()) {
            etOldPassword.error = "Masukkan password lama"
            return
        }
        if (newPassword.length < 6) {
            etNewPassword.error = "Password baru minimal 6 karakter"
            return
        }

        lifecycleScope.launch {
            val result = authRepository.changePassword(userId, oldPassword, newPassword)
            result.onSuccess {
                etOldPassword.text.clear()
                etNewPassword.text.clear()
                Toast.makeText(this@ProfileActivity, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@ProfileActivity, it.message ?: "Gagal mengubah password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val userId = session.getUserId()
        val username = etUsername.text.toString().trim()
        val ageText = etAge.text.toString().trim()

        if (username.isEmpty()) {
            etUsername.error = "Username tidak boleh kosong"
            return
        }

        val age: Int? = if (ageText.isNotEmpty()) {
            val parsedAge = ageText.toIntOrNull()
            when {
                parsedAge == null -> {
                    etAge.error = "Umur harus berupa angka"
                    return
                }
                parsedAge < MIN_AGE || parsedAge > MAX_AGE -> {
                    etAge.error = "Umur harus antara $MIN_AGE-$MAX_AGE tahun"
                    return
                }
                else -> parsedAge
            }
        } else null

        lifecycleScope.launch {
            val result = authRepository.updateProfile(userId, username, age, currentPhotoUri)
            result.onSuccess {
                session.saveSession(userId, username, session.isGuest())
                Toast.makeText(this@ProfileActivity, "Profil disimpan 💙", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@ProfileActivity, it.message ?: "Gagal menyimpan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMoodStats() {
        val userId = session.getUserId()
        val thirtyDaysAgo = System.currentTimeMillis() - (STATS_WINDOW_DAYS * MILLIS_PER_DAY)

        lifecycleScope.launch {
            val stats = journalRepository.getMoodStats(userId, thirtyDaysAgo)
            renderChart(stats)
        }
    }

    private fun renderChart(stats: List<MoodCount>) {
        layoutMoodChart.removeAllViews()
        val countMap = stats.associateBy({ it.mood_tag }, { it.count })
        val maxCount = (countMap.values.maxOrNull() ?: 1).coerceAtLeast(1)
        val maxBarHeightDp = MAX_BAR_HEIGHT_DP

        // Pakai MOODS dari single source of truth
        Mood.MOODS.forEach { mood ->
            val count = countMap[mood.id] ?: 0

            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val barHeightDp = if (count == 0) 0 else (count.toFloat() / maxCount * maxBarHeightDp).toInt().coerceAtLeast(8)
            val barHeightPx = (barHeightDp * resources.displayMetrics.density).toInt()

            val bar = View(this).apply {
                setBackgroundColor(androidx.core.content.ContextCompat.getColor(this@ProfileActivity, R.color.profile_accent))
                layoutParams = LinearLayout.LayoutParams(
                    (24 * resources.displayMetrics.density).toInt(),
                    barHeightPx
                )
            }

            val emoji = TextView(this).apply {
                text = mood.emoji
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
                viewModel.deleteAllJournalsByUser()
                authRepository.logoutGuest(userId)
            }
            session.clearSession()
            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            finish()
        }
    }
}