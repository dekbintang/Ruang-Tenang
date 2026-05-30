package com.ruangtenang.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.repository.JournalRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository

    // Bulan dan tahun yang sedang ditampilkan
    val currentCalendar = MutableLiveData<Calendar>()

    // Map dari tanggal ke mood: {"2026-05-28" -> "sad", "2026-05-15" -> "happy"}
    val monthMoodMap = MutableLiveData<Map<String, String>>()

    // Jurnal yang ditampilkan di pop-up kalender
    val selectedJournal = MutableLiveData<Journal?>()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = JournalRepository(db.journalDao())
        currentCalendar.value = Calendar.getInstance()
        loadMoodsForCurrentMonth()
    }

    fun loadMoodsForCurrentMonth() {
        val cal = currentCalendar.value ?: return
        val year  = cal.get(Calendar.YEAR)
        val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val pattern = "$year-$month-%"

        viewModelScope.launch {
            val items = repository.getMoodsForMonth(pattern)
            // Konversi list ke map untuk lookup O(1) saat render grid
            val moodMap = items.associate { it.date_string to it.mood_tag }
            monthMoodMap.postValue(moodMap)
        }
    }

    // Navigasi bulan sebelumnya
    fun goToPreviousMonth() {
        val cal = currentCalendar.value ?: Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        currentCalendar.value = cal.clone() as Calendar
        loadMoodsForCurrentMonth()
    }

    // Navigasi bulan berikutnya
    fun goToNextMonth() {
        val cal = currentCalendar.value ?: Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        currentCalendar.value = cal.clone() as Calendar
        loadMoodsForCurrentMonth()
    }

    // Dipanggil saat pengguna klik sebuah tanggal di kalender
    fun onDateClicked(dateString: String) {
        viewModelScope.launch {
            val journal = repository.getJournalByDate(dateString)
            selectedJournal.postValue(journal)
        }
    }
}
