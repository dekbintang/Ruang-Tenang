package com.ruangtenang.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ruangtenang.data.entity.Journal

@Dao
interface JournalDao {

    // ── CREATE ──────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal)

    // ── READ ─────────────────────────────────────────────────
    // Semua jurnal, diurutkan dari terbaru
    @Query("SELECT * FROM journal_table ORDER BY timestamp DESC")
    fun getAllJournals(): LiveData<List<Journal>>

    // Cari jurnal berdasarkan kata kunci judul (untuk Search Bar)
    @Query("SELECT * FROM journal_table WHERE title LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    fun searchJournals(keyword: String): LiveData<List<Journal>>

    // Ambil mood untuk semua jurnal dalam satu bulan (untuk render kalender)
    // Contoh pattern: "2026-05-%"
    @Query("SELECT date_string, mood_tag FROM journal_table WHERE date_string LIKE :monthPattern")
    suspend fun getMoodsForMonth(monthPattern: String): List<JournalCalendarItem>

    // Ambil detail jurnal berdasarkan tanggal spesifik (untuk pop-up kalender)
    // Ambil SEMUA jurnal di tanggal tertentu (bisa lebih dari satu per hari)
    @Query("SELECT * FROM journal_table WHERE date_string = :dateString ORDER BY timestamp DESC")
    suspend fun getJournalsByDate(dateString: String): List<Journal>

    // Ambil jurnal by ID (untuk halaman detail/edit)
    @Query("SELECT * FROM journal_table WHERE id = :id")
    suspend fun getJournalById(id: Int): Journal?

    // ── UPDATE ───────────────────────────────────────────────
    @Update
    suspend fun updateJournal(journal: Journal)

    // ── DELETE ───────────────────────────────────────────────
    @Delete
    suspend fun deleteJournal(journal: Journal)

    @Query("DELETE FROM journal_table WHERE id = :id")
    suspend fun deleteJournalById(id: Int)

    // Hitung total jurnal (untuk halaman profil)
    @Query("SELECT COUNT(*) FROM journal_table")
    suspend fun getCount(): Int

    // Ambil semua tanggal jurnal (distinct), diurutkan terbaru — untuk hitung streak
    @Query("SELECT DISTINCT date_string FROM journal_table ORDER BY date_string DESC")
    suspend fun getAllDateStrings(): List<String>
}

// Data class ringan khusus untuk query kalender (tidak load seluruh kolom)
data class JournalCalendarItem(
    val date_string: String,
    val mood_tag: String
)
