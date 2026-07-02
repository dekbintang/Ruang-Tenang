package com.ruangtenang.ui.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R
import com.ruangtenang.data.entity.Journal
import com.ruangtenang.data.model.Mood

class JournalAdapter(
    private val onItemClick: (Journal) -> Unit,
    private val onItemLongClick: (Journal) -> Boolean
) : ListAdapter<Journal, JournalAdapter.JournalViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMood: TextView = itemView.findViewById(R.id.iv_journal_mood)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_journal_title)
        private val tvPreview: TextView = itemView.findViewById(R.id.tv_journal_preview)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_journal_date)

        fun bind(journal: Journal) {
            val mood = Mood.fromId(journal.moodTag)
            tvMood.text = mood.emoji

            tvTitle.text = journal.title
            tvPreview.text = journal.content
            tvDate.text = formatDate(journal.dateString)

            itemView.setOnClickListener { onItemClick(journal) }
            itemView.setOnLongClickListener { onItemLongClick(journal) }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val parts = dateString.split("-")
                val year = parts[0]
                val month = when (parts[1]) {
                    "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"
                    "04" -> "Apr"; "05" -> "Mei"; "06" -> "Jun"
                    "07" -> "Jul"; "08" -> "Agu"; "09" -> "Sep"
                    "10" -> "Okt"; "11" -> "Nov"; "12" -> "Des"
                    else -> parts[1]
                }
                val day = parts[2].trimStart('0')
                "$day $month $year"
            } catch (e: Exception) {
                dateString
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Journal>() {
            override fun areItemsTheSame(oldItem: Journal, newItem: Journal): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: Journal, newItem: Journal): Boolean {
                return oldItem == newItem
            }
        }
    }
}