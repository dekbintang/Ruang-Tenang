package com.ruangtenang.data.repository

import androidx.lifecycle.LiveData
import com.ruangtenang.data.db.JournalCalendarItem
import com.ruangtenang.data.db.JournalDao
import com.ruangtenang.data.entity.Journal

class JournalRepository(private val journalDao: JournalDao) {

    fun getAllJournals(userId: Int): LiveData<List<Journal>> {
        return journalDao.getAllJournals(userId)
    }

    fun searchJournals(userId: Int, keyword: String): LiveData<List<Journal>> {
        return journalDao.searchJournals(userId, keyword)
    }

    suspend fun getMoodsForMonth(userId: Int, monthPattern: String): List<JournalCalendarItem> {
        return journalDao.getMoodsForMonth(userId, monthPattern)
    }

    suspend fun getJournalsByDate(userId: Int, dateString: String): List<Journal> {
        return journalDao.getJournalsByDate(userId, dateString)
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

    suspend fun deleteAllByUser(userId: Int) {
        journalDao.deleteAllByUser(userId)
    }
}