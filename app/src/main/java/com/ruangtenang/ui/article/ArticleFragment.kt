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
import androidx.recyclerview.widget.DividerItemDecoration
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

        // Divider hanya antara item compact (posisi 2+)
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        rvArticles.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val pos = parent.getChildAdapterPosition(view)
                // Hanya compact rows (pos >= 2) yang dapat divider bawah
                if (pos >= 2) {
                    outRect.bottom = 0
                }
            }
        })

        // Tombol Coba Lagi
        btnRetry.setOnClickListener { viewModel.refreshArticles() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
            rvArticles.visibility    = if (loading) View.GONE   else View.VISIBLE
            if (loading) layoutError.visibility = View.GONE
        }

        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            if (articles.isNullOrEmpty()) return@observe
            adapter.submitList(articles)
            layoutError.visibility = View.GONE
            rvArticles.visibility  = View.VISIBLE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            if (adapter.itemCount > 0) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
            } else {
                tvError.text           = message
                layoutError.visibility = View.VISIBLE
                rvArticles.visibility  = View.GONE
            }
        }
    }
}
