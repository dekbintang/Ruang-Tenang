package com.ruangtenang.ui.counseling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R

/**
 * Data class untuk merepresentasikan satu pesan chat.
 * @param message Isi pesan
 * @param isFromUser true jika pesan dari pengguna, false jika dari dokter
 * @param time Waktu pesan dikirim
 */
data class ChatMessage(
    val message: String,
    val isFromUser: Boolean,
    val time: String
)

class ChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutReceived: View = itemView.findViewById(R.id.layout_received)
        val layoutSent: View = itemView.findViewById(R.id.layout_sent)
        val tvMessageReceived: TextView = itemView.findViewById(R.id.tv_message_received)
        val tvMessageSent: TextView = itemView.findViewById(R.id.tv_message_sent)
        val tvTimeReceived: TextView = itemView.findViewById(R.id.tv_time_received)
        val tvTimeSent: TextView = itemView.findViewById(R.id.tv_time_sent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]

        if (msg.isFromUser) {
            holder.layoutSent.visibility = View.VISIBLE
            holder.layoutReceived.visibility = View.GONE
            holder.tvMessageSent.text = msg.message
            holder.tvTimeSent.text = msg.time
        } else {
            holder.layoutReceived.visibility = View.VISIBLE
            holder.layoutSent.visibility = View.GONE
            holder.tvMessageReceived.text = msg.message
            holder.tvTimeReceived.text = msg.time
        }
    }

    override fun getItemCount(): Int = messages.size
}
