package com.ruangtenang.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ruangtenang.R
import com.ruangtenang.data.remote.GNewsArticle
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ArticleAdapter — multi-viewtype layout:
 *   VIEW_TYPE_HEADER  (0) : Satu baris berisi label "Featured Articles" +
 *                           HorizontalScrollView yang menampung 3 kartu featured.
 *   VIEW_TYPE_SECTION (1) : Label teks "Recommended For You" sebelum list compact.
 *   VIEW_TYPE_COMPACT (2) : Baris compact (thumbnail kiri, teks kanan) untuk sisa artikel.
 *
 * Struktur posisi adapter (misalnya total 10 artikel dari API):
 *   pos 0           → HEADER  (menampilkan artikel[0..2] sebagai featured)
 *   pos 1           → SECTION label "Recommended For You"
 *   pos 2..9        → COMPACT untuk artikel[3..10]
 */
class ArticleAdapter(
    private val onItemClick: (GNewsArticle) -> Unit
) : ListAdapter<GNewsArticle, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val VIEW_TYPE_HEADER  = 0
        private const val VIEW_TYPE_SECTION = 1
        private const val VIEW_TYPE_COMPACT = 2

        /** Berapa artikel yang masuk section featured */
        private const val FEATURED_COUNT = 3

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GNewsArticle>() {
            override fun areItemsTheSame(a: GNewsArticle, b: GNewsArticle) = a.url == b.url
            override fun areContentsTheSame(a: GNewsArticle, b: GNewsArticle) = a == b
        }
    }

    // ── Mapping posisi adapter → index artikel ──────────────────────────
    // pos 0        = HEADER  (tidak ada artikel tunggal, hanya wadah featured)
    // pos 1        = SECTION label
    // pos 2..n+1   = COMPACT untuk artikel[FEATURED_COUNT .. lastIndex]
    private fun articleIndexForCompact(adapterPos: Int): Int = adapterPos - 2 + FEATURED_COUNT

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        if (count == 0) return 0
        // Header + Section + (count - FEATURED_COUNT) compact rows
        val compactCount = maxOf(0, count - FEATURED_COUNT)
        return 1 + 1 + compactCount
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0    -> VIEW_TYPE_HEADER
        1    -> VIEW_TYPE_SECTION
        else -> VIEW_TYPE_COMPACT
    }

    // ── Create ViewHolders ───────────────────────────────────────────────
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_article_header, parent, false)
            )
            VIEW_TYPE_SECTION -> SectionViewHolder(
                inflater.inflate(R.layout.item_article_section, parent, false)
            )
            else -> CompactViewHolder(
                inflater.inflate(R.layout.item_article_compact, parent, false)
            )
        }
    }

    // ── Bind ViewHolders ────────────────────────────────────────────────
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // Ambil artikel 0..FEATURED_COUNT-1
                val featuredList = (0 until minOf(FEATURED_COUNT, currentList.size))
                    .map { currentList[it] }
                holder.bind(featuredList, onItemClick)
            }
            is SectionViewHolder -> holder.bind("Recommended For You")
            is CompactViewHolder -> {
                val idx = articleIndexForCompact(position)
                if (idx < currentList.size) holder.bind(currentList[idx], onItemClick)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // HEADER ViewHolder — menampilkan featured cards dalam HorizontalScroll
    // ════════════════════════════════════════════════════════════════════
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val container: LinearLayout = view.findViewById(R.id.ll_featured_container)

        fun bind(articles: List<GNewsArticle>, onClick: (GNewsArticle) -> Unit) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(itemView.context)
            articles.forEach { article ->
                val card = inflater.inflate(R.layout.item_article_featured, container, false)
                val ivThumb  = card.findViewById<ImageView>(R.id.iv_article_thumbnail)
                val tvTitle  = card.findViewById<TextView>(R.id.tv_article_title)
                val tvSource = card.findViewById<TextView>(R.id.tv_article_source)
                val tvDate   = card.findViewById<TextView>(R.id.tv_article_date)

                tvTitle.text  = article.title
                tvSource.text = article.source.name
                tvDate.text   = formatDate(article.publishedAt)

                if (!article.image.isNullOrBlank()) {
                    Glide.with(card.context)
                        .load(article.image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.bg_article_placeholder)
                        .error(R.drawable.bg_article_placeholder)
                        .centerCrop()
                        .into(ivThumb)
                } else {
                    ivThumb.setImageResource(R.drawable.bg_article_placeholder)
                }

                card.setOnClickListener { onClick(article) }
                container.addView(card)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // SECTION ViewHolder — label teks (mis. "Recommended For You")
    // ════════════════════════════════════════════════════════════════════
    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvLabel: TextView = view.findViewById(R.id.tv_section_title)
        fun bind(label: String) { tvLabel.text = label }
    }

    // ════════════════════════════════════════════════════════════════════
    // COMPACT ViewHolder — baris thumbnail kiri + teks kanan
    // ════════════════════════════════════════════════════════════════════
    inner class CompactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivThumb:  ImageView = view.findViewById(R.id.iv_article_thumbnail)
        private val tvTitle:  TextView  = view.findViewById(R.id.tv_article_title)
        private val tvSource: TextView  = view.findViewById(R.id.tv_article_source)
        private val tvDate:   TextView  = view.findViewById(R.id.tv_article_date)

        fun bind(article: GNewsArticle, onClick: (GNewsArticle) -> Unit) {
            tvTitle.text  = article.title
            tvSource.text = article.source.name
            tvDate.text   = formatDate(article.publishedAt)

            if (!article.image.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(article.image)
                    .transform(RoundedCorners(16))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.bg_article_placeholder)
                    .error(R.drawable.bg_article_placeholder)
                    .centerCrop()
                    .into(ivThumb)
            } else {
                ivThumb.setImageResource(R.drawable.bg_article_placeholder)
            }

            itemView.setOnClickListener { onClick(article) }
        }
    }
}

// ── Shared helper — di luar class agar bisa diakses oleh semua ViewHolder ──
private fun formatDate(isoDate: String): String {
    return try {
        val inputFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val outputFmt = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
        val date      = inputFmt.parse(isoDate)
        if (date != null) outputFmt.format(date) else isoDate
    } catch (e: Exception) {
        isoDate
    }
}
