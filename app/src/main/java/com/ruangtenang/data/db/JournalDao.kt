package com.ruangtenang.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ruangtenang.data.entity.Journal

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal)

    @Query("SELECT * FROM journal_table WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getAllJournals(userId: Int): LiveData<List<Journal>>

    @Query("SELECT * FROM journal_table WHERE user_id = :userId AND title LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    fun searchJournals(userId: Int, keyword: String): LiveData<List<Journal>>

    @Query("SELECT date_string, mood_tag FROM journal_table WHERE user_id = :userId AND date_string LIKE :monthPattern")
    suspend fun getMoodsForMonth(userId: Int, monthPattern: String): List<JournalCalendarItem>

    @Query("SELECT * FROM journal_table WHERE user_id = :userId AND date_string = :dateString ORDER BY timestamp DESC")
    suspend fun getJournalsByDate(userId: Int, dateString: String): List<Journal>

    @Query("SELECT * FROM journal_table WHERE id = :id")
    suspend fun getJournalById(id: Int): Journal?

    @Update
    suspend fun updateJournal(journal: Journal)

    @Delete
    suspend fun deleteJournal(journal: Journal)

    @Query("DELETE FROM journal_table WHERE id = :id")
    suspend fun deleteJournalById(id: Int)

    @Query("SELECT COUNT(*) FROM journal_table WHERE user_id = :userId")
    suspend fun getCount(userId: Int): Int

    @Query("SELECT DISTINCT date_string FROM journal_table WHERE user_id = :userId ORDER BY date_string DESC")
    suspend fun getAllDateStrings(userId: Int): List<String>

    @Query("DELETE FROM journal_table WHERE user_id = :userId")
    suspend fun deleteAllByUser(userId: Int)

    @Query("""
        SELECT mood_tag, COUNT(*) as count 
        FROM journal_table 
        WHERE user_id = :userId AND timestamp >= :sinceTimestamp 
        GROUP BY mood_tag
    """)
    suspend fun getMoodStats(userId: Int, sinceTimestamp: Long): List<MoodCount>
}

data class JournalCalendarItem(
    val date_string: String,
    val mood_tag: String
)

data class MoodCount(
    val mood_tag: String,
    val count: Int
)