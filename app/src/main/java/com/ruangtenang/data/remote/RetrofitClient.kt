package com.ruangtenang.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client untuk GNews API.
 * Hanya dibuat sekali selama lifetime aplikasi.
 */
object RetrofitClient {

    private const val BASE_URL = "https://gnews.io/api/v4/"
    private const val TIMEOUT_SECONDS = 30L

    // OkHttpClient dengan logging (hanya tampil di debug build)
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // Instance Retrofit — dibuat sekali, reuse selama app berjalan
    val gNewsApi: GNewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GNewsApiService::class.java)
    }
}
