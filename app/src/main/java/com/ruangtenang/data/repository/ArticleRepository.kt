package com.ruangtenang.data.repository

import android.content.Context
import com.ruangtenang.BuildConfig
import com.ruangtenang.data.db.AffirmationDao
import com.ruangtenang.data.entity.Affirmation
import com.ruangtenang.data.remote.GNewsArticle
import com.ruangtenang.data.remote.GNewsSource
import com.ruangtenang.data.remote.RetrofitClient
import org.json.JSONArray

/**
 * Repository untuk artikel dari GNews API.
 *
 * Strategi:
 * 1. Cek cache SharedPreferences (berlaku 1 jam)
 * 2. Jika cache expired → fetch dari GNews API
 * 3. Jika API key kosong/invalid atau jaringan gagal → fallback ke seed_articles.json di assets
 *
 * Dengan fallback ini, artikel tetap tampil meski API key belum dikonfigurasi.
 */
class ArticleRepository(
    private val context: Context,
    private val affirmationDao: AffirmationDao
) {
    companion object {
        private const val PREF_NAME         = "gnews_cache_v4"
        private const val KEY_CACHE_TIME    = "cache_timestamp"
        private const val KEY_CACHED_JSON   = "cached_json"
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L // 1 jam
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ── Fetch artikel: cache → network → seed fallback ───────────────────────
    suspend fun fetchArticles(): Result<List<GNewsArticle>> {
        // 1. Cek cache
        val cachedJson = getCachedJson()
        if (cachedJson != null) {
            return try {
                val type = object : com.google.gson.reflect.TypeToken<List<GNewsArticle>>() {}.type
                val cached: List<GNewsArticle> = com.google.gson.Gson().fromJson(cachedJson, type)
                if (cached.isNotEmpty()) Result.success(cached) else fetchFromNetworkOrSeed()
            } catch (e: Exception) {
                fetchFromNetworkOrSeed()
            }
        }

        // 2. Cache expired/tidak ada → fetch dari network atau seed
        return fetchFromNetworkOrSeed()
    }

    private suspend fun fetchFromNetworkOrSeed(): Result<List<GNewsArticle>> {
        val apiKey = BuildConfig.GNEWS_API_KEY

        // Jika API key belum dikonfigurasi → langsung pakai seed
        if (apiKey.isBlank() || apiKey == "ISI_API_KEY_DISINI") {
            return loadSeedArticles()
        }

        return try {
            val response = RetrofitClient.gNewsApi.searchArticles(apiKey = apiKey)
            val articles = response.articles

            if (articles.isNotEmpty()) {
                saveToCache(articles)
                Result.success(articles)
            } else {
                // API sukses tapi artikel kosong → pakai seed
                loadSeedArticles()
            }
        } catch (e: retrofit2.HttpException) {
            // Error HTTP (401, 429, dsb) → fallback ke seed
            loadSeedArticles()
        } catch (e: java.net.UnknownHostException) {
            // Tidak ada internet → fallback ke seed
            loadSeedArticles()
        } catch (e: java.net.SocketTimeoutException) {
            // Timeout → fallback ke seed
            loadSeedArticles()
        } catch (e: Exception) {
            // Error lain → coba seed, kalau seed juga gagal baru return failure
            val seed = loadSeedArticles()
            if (seed.isSuccess && (seed.getOrNull()?.isNotEmpty() == true)) seed
            else Result.failure(Exception("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }

    // ── Muat artikel dari assets/seed_articles.json ──────────────────────────
    //
    // Struktur seed_articles.json berbeda dari GNewsArticle (field lokal seperti
    // "category", "emoji_icon", "summary", "content" plain text, tanpa "url" eksternal).
    // Kita peta ulang ke GNewsArticle agar adapter tidak perlu diubah.
    private fun loadSeedArticles(): Result<List<GNewsArticle>> {
        return try {
            val json = context.assets.open("seed_articles.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(json)
            val articles = mutableListOf<GNewsArticle>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                // Seed pakai "summary" sebagai deskripsi singkat
                val description = obj.optString("summary").takeIf { it.isNotBlank() }

                // Seed pakai "content" sebagai isi artikel
                val content = obj.optString("content").takeIf { it.isNotBlank() }

                // Seed tidak punya URL eksternal — pakai string kosong
                // ArticleWebViewActivity akan mendeteksi ini dan menampilkan content lokal
                val url = obj.optString("url", "")

                // Seed mungkin punya "image_url" atau tidak ada gambar
                val image = obj.optString("image_url", "")
                    .takeIf { it.isNotBlank() }

                // Kategori artikel dijadikan "sumber"
                val category = obj.optString("category", "Kesehatan Mental")
                val source = GNewsSource(name = category, url = "")

                // Tanggal publikasi — seed tidak punya, pakai string kosong
                val publishedAt = obj.optString("publishedAt", "2024-01-01T00:00:00Z")

                articles.add(
                    GNewsArticle(
                        title       = obj.optString("title", ""),
                        description = description,
                        content     = content,
                        url         = url,
                        image       = image,
                        publishedAt = publishedAt,
                        source      = source
                    )
                )
            }

            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat artikel. Coba lagi nanti."))
        }
    }

    // ── Cache Helpers ────────────────────────────────────────────────────────

    private fun getCachedJson(): String? {
        val cacheTime = prefs.getLong(KEY_CACHE_TIME, 0L)
        val now       = System.currentTimeMillis()
        if (now - cacheTime > CACHE_DURATION_MS) return null
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