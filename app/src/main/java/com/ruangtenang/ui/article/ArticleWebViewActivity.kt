package com.ruangtenang.ui.article

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ruangtenang.R

/**
 * Activity untuk membaca artikel di WebView in-app.
 *
 * Menerima extra:
 *   - EXTRA_URL   : String — URL artikel yang akan dibuka
 *   - EXTRA_TITLE : String — Judul artikel untuk ditampilkan di toolbar
 */
class ArticleWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL   = "extra_article_url"
        const val EXTRA_TITLE = "extra_article_title"
    }

    private lateinit var webView:      WebView
    private lateinit var progressBar:  ProgressBar
    private lateinit var tvTitle:      TextView
    private lateinit var btnBack:      ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_webview)

        // Bind views
        webView     = findViewById(R.id.webview_article)
        progressBar = findViewById(R.id.progress_webview)
        tvTitle     = findViewById(R.id.tv_webview_title)
        btnBack     = findViewById(R.id.btn_back_webview)

        // Ambil data dari Intent
        val url   = intent.getStringExtra(EXTRA_URL)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Artikel"

        // Tampilkan judul
        tvTitle.text = title

        // Tombol back
        btnBack.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
            else finish()
        }

        if (url.isNullOrBlank()) {
            finish()
            return
        }

        setupWebView()
        webView.loadUrl(url)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true
            setSupportZoom(true)
            builtInZoomControls   = true
            displayZoomControls   = false
            loadWithOverviewMode  = true
            useWideViewPort       = true
        }

        // Update progress bar saat halaman loading
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress  = newProgress
                progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }

        // Handle navigasi dan error
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    return false // Biarkan WebView yang menangani
                }
                return true // Abaikan link aneh (misal: intent://, whatsapp://)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // Tampilkan halaman error sederhana
                webView.loadData(
                    """
                    <html><body style="font-family:sans-serif;text-align:center;padding:40px;color:#4A5568;">
                        <h2>😔</h2>
                        <h3>Gagal memuat halaman</h3>
                        <p>Periksa koneksi internet kamu dan coba lagi.</p>
                    </body></html>
                    """.trimIndent(),
                    "text/html",
                    "UTF-8"
                )
            }
        }
    }

    // Handle tombol back fisik → navigasi WebView dulu sebelum keluar
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
