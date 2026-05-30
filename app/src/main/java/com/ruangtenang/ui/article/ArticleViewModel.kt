package com.ruangtenang.ui.article

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.remote.GNewsArticle
import com.ruangtenang.data.repository.ArticleRepository
import kotlinx.coroutines.launch

class ArticleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArticleRepository

    // ── LiveData yang di-observe oleh ArticleFragment ────────────────────────
    private val _articles     = MutableLiveData<List<GNewsArticle>>()
    val articles: LiveData<List<GNewsArticle>> = _articles

    private val _isLoading    = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ArticleRepository(application.applicationContext, db.affirmationDao())
        loadArticles()
    }

    fun loadArticles() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = repository.fetchArticles()
            _isLoading.postValue(false)

            result.fold(
                onSuccess = { articles ->
                    _articles.postValue(articles)
                },
                onFailure = { error ->
                    _errorMessage.postValue(error.message)
                }
            )
        }
    }

    /** Paksa refresh ulang dari network (bypass cache) */
    fun refreshArticles() {
        loadArticles()
    }
}
