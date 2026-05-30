package com.ruangtenang.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ruangtenang.BuildConfig
import com.ruangtenang.data.db.AffirmationDao
import com.ruangtenang.data.entity.Affirmation
import com.ruangtenang.data.remote.GNewsArticle
import com.ruangtenang.data.remote.RetrofitClient

/**
 * Repository untuk artikel dari GNews API.
 *
 * Strategi caching:
 * - Hasil fetch disimpan sementara di SharedPreferences (sebagai JSON string)
 * - Cache berlaku selama 1 jam untuk menghemat quota GNews free tier (100 req/hari)
 * - Jika gagal fetch dan tidak ada cache → return Result.failure dengan pesan yang ramah
 */
class ArticleRepository(
    private val context: Context,
    private val affirmationDao: AffirmationDao
) {
    companion object {
        private const val PREF_NAME          = "gnews_cache_v4"
        private const val KEY_CACHE_TIME     = "cache_timestamp"
        private const val KEY_CACHED_JSON    = "cached_json"
        private const val CACHE_DURATION_MS  = 60 * 60 * 1000L // 1 jam
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ── Fetch artikel dari GNews API (dengan caching 1 jam) ──────────────────
    suspend fun fetchArticles(): Result<List<GNewsArticle>> {
        // 1. Cek apakah cache masih fresh (< 1 jam)
        val cachedJson = getCachedJson()
        if (cachedJson != null) {
            return try {
                val type = object : com.google.gson.reflect.TypeToken<List<GNewsArticle>>() {}.type
                val cached: List<GNewsArticle> = com.google.gson.Gson().fromJson(cachedJson, type)
                Result.success(cached)
            } catch (e: Exception) {
                fetchFromNetwork()
            }
        }

        // 2. Cache expired/tidak ada → fetch dari network
        return fetchFromNetwork()
    }

    private suspend fun fetchFromNetwork(): Result<List<GNewsArticle>> {
        return try {
            val apiKey = BuildConfig.GNEWS_API_KEY
            if (apiKey.isBlank() || apiKey == "ISI_API_KEY_DISINI") {
                return Result.failure(Exception("API key belum dikonfigurasi. Masukkan GNEWS_API_KEY di local.properties."))
            }

            val response = RetrofitClient.gNewsApi.searchArticles(apiKey = apiKey)
            val articles = response.articles

            // Simpan ke cache
            saveToCache(articles)

            Result.success(articles)
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                401 -> "API key tidak valid. Periksa kembali GNEWS_API_KEY di local.properties."
                429 -> "Batas request harian tercapai. Coba lagi besok."
                else -> "Gagal memuat artikel (HTTP ${e.code()})"
            }
            Result.failure(Exception(msg))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Tidak ada koneksi internet. Periksa jaringanmu."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Koneksi timeout. Coba lagi sebentar."))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }

    // ── Cache Helpers ────────────────────────────────────────────────────────

    private fun getCachedJson(): String? {
        val cacheTime = prefs.getLong(KEY_CACHE_TIME, 0L)
        val now       = System.currentTimeMillis()
        if (now - cacheTime > CACHE_DURATION_MS) return null // cache kedaluwarsa
        return prefs.getString(KEY_CACHED_JSON, null)
    }

    private fun saveToCache(articles: List<GNewsArticle>) {
        val json = com.google.gson.Gson().toJson(articles)
        prefs.edit()
            .putString(KEY_CACHED_JSON, json)
            .putLong(KEY_CACHE_TIME, System.currentTimeMillis())
            .apply()
    }

    // ── Afirmasi (tetap dari Room DB) ────────────────────────────────────────
    suspend fun getRandomAffirmation(): Affirmation? {
        return affirmationDao.getRandomAffirmation()
    }
}
