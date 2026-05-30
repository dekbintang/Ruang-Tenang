package com.ruangtenang.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit untuk GNews API
 * Base URL: https://gnews.io/api/v4/
 */
interface GNewsApiService {

    /**
     * Cari artikel berdasarkan keyword
     * @param query    Kata kunci pencarian (default: kesehatan mental)
     * @param lang     Bahasa artikel (id = Indonesia, en = English)
     * @param country  Negara (id = Indonesia)
     * @param max      Jumlah artikel (maks 10 di free tier)
     * @param apiKey   API key dari gnews.io
     */
    @GET("search")
    suspend fun searchArticles(
        @Query("q")      query: String   = "kesehatan",
        @Query("lang")   lang: String    = "id",
        @Query("max")    max: Int        = 10,
        @Query("apikey") apiKey: String
    ): GNewsResponse
}
