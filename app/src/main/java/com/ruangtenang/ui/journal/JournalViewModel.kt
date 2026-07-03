package com.ruangtenang.ui.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.ruangtenang.data.SessionManager
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.repository.JournalRepository
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository = JournalRepository(
        AppDatabase.getDatabase(application).journalDao()
    )

    private val session = SessionManager(application)
    private val userId: Int = session.getUserId()

    val allJournals: LiveData<List<Journal>> = repository.getAllJournals(userId)

    private val _searchQuery = MutableLiveData<String>("")
    private val _selectedMood = MutableLiveData<String?>(null)

    val selectedMood: LiveData<String?> = _selectedMood

    private val filterTrigger = MediatorLiveData<Pair<String, String?>>().apply {
        value = Pair(_searchQuery.value ?: "", _selectedMood.value)
        addSource(_searchQuery) { keyword -> value = Pair(keyword, _selectedMood.value) }
        addSource(_selectedMood) { mood -> value = Pair(_searchQuery.value ?: "", mood) }
    }

    val searchResults: LiveData<List<Journal>> = filterTrigger.switchMap { (keyword, mood) ->
        repository.searchAndFilterJournals(userId, keyword, mood)
    }

    fun setSearchQuery(keyword: String) {
        _searchQuery.value = keyword
    }

    fun setMoodFilter(moodId: String?) {
        _selectedMood.value = moodId
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

    suspend fun getJournalById(id: Int): Journal? {
        return repository.getJournalById(id)
    }

    suspend fun getMoodsForMonth(monthPattern: String) =
        repository.getMoodsForMonth(userId, monthPattern)

    suspend fun getJournalsByDate(dateString: String) =
        repository.getJournalsByDate(userId, dateString)

    suspend fun deleteAllJournalsByUser() {
        repository.deleteAllByUser(userId)
    }
}