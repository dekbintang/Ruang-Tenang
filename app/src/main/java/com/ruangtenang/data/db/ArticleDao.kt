package com.ruangtenang.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ruangtenang.data.entity.Article

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<Article>)

    // Semua artikel (data statis, diurutkan by ID)
    @Query("SELECT * FROM article_table ORDER BY id ASC")
    fun getAllArticles(): LiveData<List<Article>>

    // Filter berdasarkan kategori (opsional, untuk chip filter)
    @Query("SELECT * FROM article_table WHERE category = :category ORDER BY id ASC")
    fun getArticlesByCategory(category: String): LiveData<List<Article>>

    // Cek apakah data artikel sudah ada (untuk mencegah seeding berulang)
    @Query("SELECT COUNT(*) FROM article_table")
    suspend fun getArticleCount(): Int

    @Query("SELECT * FROM article_table WHERE id = :id")
    suspend fun getArticleById(id: Int): Article?
}
