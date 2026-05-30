package com.ruangtenang.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ruangtenang.R
import com.ruangtenang.data.remote.GNewsArticle
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter sederhana untuk menampilkan artikel di Dashboard (horizontal scroll).
 * Menggunakan item_article_featured.xml (card dengan gambar besar).
 */
class DashboardArticleAdapter(
    private val onItemClick: (GNewsArticle) -> Unit
) : ListAdapter<GNewsArticle, DashboardArticleAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_featured, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        private fun formatDate(isoDate: String): String = try {
            val inputFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val outputFmt = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
            val date      = inputFmt.parse(isoDate)
            if (date != null) outputFmt.format(date) else isoDate
        } catch (e: Exception) { isoDate }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GNewsArticle>() {
            override fun areItemsTheSame(a: GNewsArticle, b: GNewsArticle) = a.url == b.url
            override fun areContentsTheSame(a: GNewsArticle, b: GNewsArticle) = a == b
        }
    }
}
