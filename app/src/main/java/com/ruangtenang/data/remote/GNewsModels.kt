package com.ruangtenang.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Model response dari GNews API
 * Dokumentasi: https://gnews.io/docs/v4
 */
data class GNewsResponse(
    @SerializedName("totalArticles") val totalArticles: Int,
    @SerializedName("articles")      val articles: List<GNewsArticle>
)

data class GNewsArticle(
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String?,  // ringkasan artikel
    @SerializedName("content")     val content: String?,      // isi artikel (bisa panjang)
    @SerializedName("url")         val url: String,           // URL untuk WebView
    @SerializedName("image")       val image: String?,        // URL thumbnail gambar
    @SerializedName("publishedAt") val publishedAt: String,   // tanggal publikasi
    @SerializedName("source")      val source: GNewsSource
)

data class GNewsSource(
    @SerializedName("name") val name: String,
    @SerializedName("url")  val url: String
)
