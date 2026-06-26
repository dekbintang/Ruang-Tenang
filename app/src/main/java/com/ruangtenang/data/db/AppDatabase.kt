package com.ruangtenang.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ruangtenang.data.entity.Journal

/**
 * Room Database RuangTenang
 *
 * Versi 4: Aplikasi disederhanakan menjadi Diary CRUD murni.
 * Hanya entitas Journal yang digunakan.
 */
@Database(
    entities = [Journal::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    companion object {
        // Singleton — mencegah multiple instance database terbuka sekaligus
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruang_tenang_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
