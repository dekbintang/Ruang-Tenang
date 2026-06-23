package com.ruangtenang.ui.counseling

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruangtenang.R
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.entity.ChatMessageEntity
import com.ruangtenang.data.entity.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
    }

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView

    private var sessionId: Int = -1
    private lateinit var doctorName: String
    private lateinit var doctorEmoji: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        doctorName = intent.getStringExtra("doctor_name") ?: "Dokter"
        doctorEmoji = intent.getStringExtra("doctor_emoji") ?: "👨‍⚕️"
        sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)

        // Set header
        findViewById<TextView>(R.id.tv_chat_doctor_name).text = doctorName
        findViewById<TextView>(R.id.tv_chat_doctor_emoji).text = doctorEmoji

        // Tombol kembali
        findViewById<ImageButton>(R.id.btn_back_chat).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.rv_chat_messages)
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter

        // Setup kirim pesan
        val etMessage = findViewById<EditText>(R.id.et_chat_message)
        val btnSend = findViewById<FloatingActionButton>(R.id.btn_send_message)

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                addUserMessage(message)
                etMessage.text?.clear()

                // Simulasi balasan dokter setelah 1-2 detik
                Handler(Looper.getMainLooper()).postDelayed({
                    replyFromDoctor(message)
                }, 1500)
            }
        }

        // Load session atau buat baru
        loadOrCreateSession()
    }

    private fun loadOrCreateSession() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            if (sessionId != -1) {
                // Memuat riwayat chat dari database
                val savedMessages = withContext(Dispatchers.IO) {
                    db.chatDao().getMessagesForSession(sessionId)
                }
                savedMessages.forEach { msg ->
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(msg.timestamp))
                    chatMessages.add(ChatMessage(msg.message, msg.isFromUser, timeStr))
                }
                chatAdapter.notifyDataSetChanged()
                if (chatMessages.isNotEmpty()) {
                    recyclerView.scrollToPosition(chatMessages.size - 1)
                }
            } else {
                // Buat sesi baru
                val newSession = ChatSession(
                    doctorName = doctorName,
                    doctorEmoji = doctorEmoji
                )
                sessionId = withContext(Dispatchers.IO) {
                    db.chatDao().insertSession(newSession).toInt()
                }

                // Pesan selamat datang dari dokter
                addDoctorMessage("Halo! Saya $doctorName. 😊\nSelamat datang di sesi konseling. Ada yang bisa saya bantu hari ini?")
            }
        }
    }

    private fun addUserMessage(message: String) {
        val time = getCurrentTime()
        chatMessages.add(ChatMessage(message, isFromUser = true, time = time))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)

        // Simpan ke database
        saveMessageToDb(message, isFromUser = true)
    }

    private fun addDoctorMessage(message: String) {
        val time = getCurrentTime()
        chatMessages.add(ChatMessage(message, isFromUser = false, time = time))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)

        // Simpan ke database
        saveMessageToDb(message, isFromUser = false)
    }

    private fun saveMessageToDb(message: String, isFromUser: Boolean) {
        if (sessionId == -1) return
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.chatDao().insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    message = message,
                    isFromUser = isFromUser
                )
            )
            // Update last message di session
            db.chatDao().updateSessionLastMessage(sessionId, message)
        }
    }

    /**
     * Simulasi balasan otomatis dari dokter berdasarkan kata kunci.
     */
    private fun replyFromDoctor(userMessage: String) {
        val lowerMsg = userMessage.lowercase()
        val reply = when {
            lowerMsg.contains("sedih") || lowerMsg.contains("galau") ->
                "Saya mengerti perasaanmu. Bisa ceritakan lebih detail apa yang membuatmu merasa sedih? 💙"
            lowerMsg.contains("cemas") || lowerMsg.contains("takut") || lowerMsg.contains("khawatir") ->
                "Kecemasan itu wajar. Mari kita coba teknik pernapasan: tarik nafas 4 detik, tahan 4 detik, buang 4 detik. Kamu aman di sini. 🍃"
            lowerMsg.contains("marah") || lowerMsg.contains("kesal") ->
                "Emosi marah itu valid. Yang penting adalah bagaimana kita mengelolanya. Apa yang membuatmu merasa kesal? 🤝"
            lowerMsg.contains("stres") || lowerMsg.contains("stress") || lowerMsg.contains("capek") ->
                "Stres bisa sangat melelahkan. Sudahkah kamu istirahat yang cukup hari ini? Kesehatan fisik juga memengaruhi mental kita. 💆"
            lowerMsg.contains("terima kasih") || lowerMsg.contains("makasih") ->
                "Sama-sama! Senang bisa membantu. Ingat, kamu tidak sendirian. Aku selalu di sini untukmu. 😊"
            lowerMsg.contains("halo") || lowerMsg.contains("hai") || lowerMsg.contains("hi") ->
                "Hai! Senang bertemu denganmu. Bagaimana kabarmu hari ini? 😊"
            else ->
                "Terima kasih sudah bercerita. Perasaanmu sangat valid. Apakah ada hal lain yang ingin kamu sampaikan? Saya di sini untuk mendengarkan. 💛"
        }
        addDoctorMessage(reply)
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
