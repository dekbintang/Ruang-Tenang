package com.ruangtenang.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.entity.User

@Database(
    entities = [Journal::class, User::class],
    version = 5, // naik dari 4 ke 5
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruang_tenang_database"
                )
                    .fallbackToDestructiveMigration() // aman untuk development, data lama akan terhapus saat versi naik
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}