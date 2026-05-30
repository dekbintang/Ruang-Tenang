package com.ruangtenang.ui.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.repository.JournalRepository
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi repository di sini dulu, SEBELUM dipakai oleh switchMap
    private val repository: JournalRepository = JournalRepository(
        AppDatabase.getDatabase(application).journalDao()
    )

    // Semua jurnal (LiveData — otomatis update RecyclerView)
    val allJournals: LiveData<List<Journal>> = repository.allJournals

    // Query pencarian — reactive terhadap perubahan keyword
    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Journal>> = _searchQuery.switchMap { keyword ->
        if (keyword.isBlank()) {
            repository.allJournals
        } else {
            repository.searchJournals(keyword)
        }
    }

    fun setSearchQuery(keyword: String) {
        _searchQuery.value = keyword
    }

    fun insertJournal(journal: Journal) = viewModelScope.launch {
        repository.insertJournal(journal)
    }

    fun updateJournal(journal: Journal) = viewModelScope.launch {
        repository.updateJournal(journal)
    }

    fun deleteJournal(journal: Journal) = viewModelScope.launch {
        repository.deleteJournal(journal)
    }

    fun deleteJournalById(id: Int) = viewModelScope.launch {
        repository.deleteJournalById(id)
    }
}
