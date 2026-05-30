package com.ruangtenang.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ruangtenang.data.entity.Affirmation
import com.ruangtenang.data.entity.Journal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database RuangTenang
 *
 * Versi 2: Article tidak lagi disimpan di Room (digantikan oleh GNews API).
 * Journal dan Affirmation tetap ada di Room DB.
 *
 * Migration dari versi 1 → 2: drop tabel article_table (tidak dipakai lagi)
 */
@Database(
    entities = [Journal::class, Affirmation::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun affirmationDao(): AffirmationDao

    companion object {
        // Singleton — mencegah multiple instance database terbuka sekaligus
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration: hapus tabel article_table (tidak dipakai lagi sejak v2)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS article_table")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruang_tenang_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Database Callback: Dipanggil SEKALI saat database pertama kali dibuat
    // Hanya seeding Affirmasi (Artikel sekarang dari API)
    // ────────────────────────────────────────────────────────────────────────
    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedAffirmations(database.affirmationDao())
                }
            }
        }

        // ── Seeding Afirmasi ──────────────────────────────────────────────
        private suspend fun seedAffirmations(affirmationDao: AffirmationDao) {
            if (affirmationDao.getAffirmationCount() > 0) return

            try {
                val jsonString = context.assets
                    .open("seed_affirmations.json")
                    .bufferedReader()
                    .use { it.readText() }

                val type = object : TypeToken<List<Affirmation>>() {}.type
                val affirmations: List<Affirmation> = Gson().fromJson(jsonString, type)
                affirmationDao.insertAll(affirmations)
            } catch (e: Exception) {
                e.printStackTrace()
                affirmationDao.insertAll(getFallbackAffirmations())
            }
        }

        private fun getFallbackAffirmations(): List<Affirmation> = listOf(
            Affirmation(quote = "Kamu cukup. Tepat seperti adanya kamu sekarang.", author = "Ruang Tenang"),
            Affirmation(quote = "Selamat datang di ruang tenangmu. Di sini, kamu aman.", author = "Ruang Tenang")
        )
    }
}
