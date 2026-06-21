package com.ruangtenang.ui.article

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.ruangtenang.R

class ArticleFragment : Fragment() {

    private lateinit var viewModel: ArticleViewModel
    private lateinit var adapter: ArticleAdapter

    // Views
    private lateinit var rvArticles:    RecyclerView
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutError:   LinearLayout
    private lateinit var tvError:       TextView
    private lateinit var btnRetry:      MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        rvArticles    = view.findViewById(R.id.rv_articles)
        layoutLoading = view.findViewById(R.id.layout_loading)
        layoutError   = view.findViewById(R.id.layout_error)
        tvError       = view.findViewById(R.id.tv_error_message)
        btnRetry      = view.findViewById(R.id.btn_retry)

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[ArticleViewModel::class.java]

        // Setup adapter dengan multi-viewtype
        adapter = ArticleAdapter { article ->
            val intent = Intent(requireContext(), ArticleWebViewActivity::class.java).apply {
                putExtra(ArticleWebViewActivity.EXTRA_URL,   article.url)
                putExtra(ArticleWebViewActivity.EXTRA_TITLE, article.title)
            }
            startActivity(intent)
        }

        rvArticles.layoutManager = LinearLayoutManager(requireContext())
        rvArticles.adapter = adapter

        // Tombol Coba Lagi
        btnRetry.setOnClickListener { viewModel.refreshArticles() }

        observeViewModel()
    }

    private fun observeViewModel() {
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                rvArticles.visibility  = View.GONE
                layoutError.visibility = View.GONE
            }
        }

        // Artikel berhasil dimuat
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            // PERBAIKAN: dulu ada "if (articles.isNullOrEmpty()) return@observe"
            // yang membuat list kosong tidak pernah trigger error state.
            // Sekarang kita handle eksplisit:
            if (!articles.isNullOrEmpty()) {
                adapter.submitList(articles)
                rvArticles.visibility  = View.VISIBLE
                layoutError.visibility = View.GONE
                layoutLoading.visibility = View.GONE
            }
            // Jika kosong, biarkan errorMessage observer yang handle
        }

        // Error state
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe

            // Jika ada artikel tampil → pakai Snackbar (tidak mengganggu tampilan)
            if (adapter.itemCount > 0) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
            } else {
                // Tidak ada artikel sama sekali → tampilkan error state penuh
                tvError.text           = message
                layoutError.visibility = View.VISIBLE
                rvArticles.visibility  = View.GONE
                layoutLoading.visibility = View.GONE
            }
        }
    }
}