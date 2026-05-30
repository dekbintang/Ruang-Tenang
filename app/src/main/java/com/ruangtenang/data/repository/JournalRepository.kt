package com.ruangtenang.data.repository

import androidx.lifecycle.LiveData
import com.ruangtenang.data.db.JournalCalendarItem
import com.ruangtenang.data.db.JournalDao
import com.ruangtenang.data.entity.Journal

class JournalRepository(private val journalDao: JournalDao) {

    // LiveData otomatis update UI saat data berubah
    val allJournals: LiveData<List<Journal>> = journalDao.getAllJournals()

    fun searchJournals(keyword: String): LiveData<List<Journal>> {
        return journalDao.searchJournals(keyword)
    }

    suspend fun getMoodsForMonth(monthPattern: String): List<JournalCalendarItem> {
        return journalDao.getMoodsForMonth(monthPattern)
    }

    suspend fun getJournalByDate(dateString: String): Journal? {
        return journalDao.getJournalByDate(dateString)
    }

    suspend fun getJournalById(id: Int): Journal? {
        return journalDao.getJournalById(id)
    }

    suspend fun insertJournal(journal: Journal) {
        journalDao.insertJournal(journal)
    }

    suspend fun updateJournal(journal: Journal) {
        journalDao.updateJournal(journal)
    }

    suspend fun deleteJournal(journal: Journal) {
        journalDao.deleteJournal(journal)
    }

    suspend fun deleteJournalById(id: Int) {
        journalDao.deleteJournalById(id)
    }
}
