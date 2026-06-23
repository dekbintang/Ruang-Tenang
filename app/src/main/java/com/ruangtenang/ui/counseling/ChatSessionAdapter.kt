package com.ruangtenang.ui.counseling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R
import com.ruangtenang.data.entity.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatSessionAdapter(
    private val sessions: List<ChatSession>,
    private val onItemClick: (ChatSession) -> Unit
) : RecyclerView.Adapter<ChatSessionAdapter.SessionViewHolder>() {

    private val timeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tv_session_emoji)
        val tvDoctorName: TextView = itemView.findViewById(R.id.tv_session_doctor_name)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_session_last_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_session_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.tvEmoji.text = session.doctorEmoji
        holder.tvDoctorName.text = session.doctorName
        holder.tvLastMessage.text = session.lastMessage.ifEmpty { "Belum ada pesan" }
        holder.tvTime.text = timeFormat.format(Date(session.updatedAt))

        holder.itemView.setOnClickListener {
            onItemClick(session)
        }
    }

    override fun getItemCount(): Int = sessions.size
}
