package com.ruangtenang.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Affirmation
import com.ruangtenang.data.db.AffirmationDao
import com.ruangtenang.data.repository.JournalRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val affirmationDao: AffirmationDao
    private val journalRepository: JournalRepository

    private val _randomAffirmation = MutableLiveData<Affirmation?>()
    val randomAffirmation: LiveData<Affirmation?> = _randomAffirmation

    // LiveData untuk hasil perhitungan meteran emosi
    // Pair(Mood Dominan, Persentase)
    private val _dominantEmotion = MutableLiveData<Pair<String, Int>>()
    val dominantEmotion: LiveData<Pair<String, Int>> = _dominantEmotion

    // LiveData untuk streak hari berturut-turut menulis jurnal
    private val _currentStreak = MutableLiveData<Int>(0)
    val currentStreak: LiveData<Int> = _currentStreak

    init {
        val db = AppDatabase.getDatabase(application)
        affirmationDao = db.affirmationDao()
        journalRepository = JournalRepository(db.journalDao())

        loadRandomAffirmation()
        calculateDominantEmotion()
        calculateStreak()
    }

    fun loadRandomAffirmation() {
        viewModelScope.launch {
            _randomAffirmation.postValue(affirmationDao.getRandomAffirmation())
        }
    }

    fun calculateDominantEmotion() {
        // Ambil semua jurnal, lalu filter dan hitung secara manual
        journalRepository.allJournals.observeForever { journals ->
            if (journals.isNullOrEmpty()) {
                _dominantEmotion.value = Pair("neutral", 0)
                return@observeForever
            }

            // Batasi hanya 7 hari terakhir
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val sevenDaysAgo = calendar.timeInMillis

            val recentJournals = journals.filter { it.timestamp >= sevenDaysAgo }

            if (recentJournals.isEmpty()) {
                _dominantEmotion.value = Pair("neutral", 0)
                return@observeForever
            }

            // Hitung frekuensi setiap mood
            val moodCounts = recentJournals.groupingBy { it.moodTag }.eachCount()
            val totalJournals = recentJournals.size

            // Cari mood yang paling banyak muncul
            val dominantMood = moodCounts.maxByOrNull { it.value }?.key ?: "neutral"
            val dominantCount = moodCounts[dominantMood] ?: 0

            val percentage = ((dominantCount.toFloat() / totalJournals) * 100).toInt()

            _dominantEmotion.value = Pair(dominantMood, percentage)
        }
    }
    /**
     * Hitung berapa hari berturut-turut pengguna menulis jurnal.
     * Logika:
     *   1. Ambil semua tanggal jurnal (distinct, desc)
     *   2. Mulai dari hari ini — jika tidak ada jurnal hari ini, cek kemarin
     *   3. Hitung mundur berurutan
     */
    fun calculateStreak() {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            val dateStrings = db.journalDao().getAllDateStrings() // sorted DESC

            if (dateStrings.isEmpty()) {
                _currentStreak.postValue(0)
                return@launch
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())

            // Bangun set tanggal untuk lookup O(1)
            val dateSet = dateStrings.toHashSet()

            // Tentukan titik mulai: hari ini atau kemarin
            val cal = Calendar.getInstance()
            val startDate = when {
                dateSet.contains(today) -> today
                else -> {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    sdf.format(cal.time)
                }
            }

            // Jika titik mulai pun tidak ada jurnal, streak = 0
            if (!dateSet.contains(startDate)) {
                _currentStreak.postValue(0)
                return@launch
            }

            // Hitung mundur dari startDate
            var streak = 0
            val counter = Calendar.getInstance()
            counter.time = sdf.parse(startDate) ?: Date()

            while (dateSet.contains(sdf.format(counter.time))) {
                streak++
                counter.add(Calendar.DAY_OF_YEAR, -1)
            }

            _currentStreak.postValue(streak)
        }
    }
}
