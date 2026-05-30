package com.ruangtenang.data.db

import androidx.room.*
import com.ruangtenang.data.entity.Affirmation

@Dao
interface AffirmationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(affirmations: List<Affirmation>)

    // Ambil 1 afirmasi secara acak untuk ditampilkan di Dashboard
    @Query("SELECT * FROM affirmation_table ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomAffirmation(): Affirmation?

    // Cek apakah data sudah ada (untuk mencegah seeding berulang)
    @Query("SELECT COUNT(*) FROM affirmation_table")
    suspend fun getAffirmationCount(): Int
}
