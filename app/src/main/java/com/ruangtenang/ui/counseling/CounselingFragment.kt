package com.ruangtenang.ui.counseling

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R
import com.ruangtenang.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CounselingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_counseling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup daftar dokter
        val rvDoctors = view.findViewById<RecyclerView>(R.id.rv_doctors)
        rvDoctors.layoutManager = LinearLayoutManager(requireContext())

        val doctors = listOf(
            Doctor("Dr. Anisa Rahma, M.Psi", "Psikolog Klinis", "👩‍⚕️", 4.9, "8 Tahun", 150_000),
            Doctor("Dr. Budi Santoso, M.Psi", "Psikolog Anak & Remaja", "👨‍⚕️", 4.8, "12 Tahun", 175_000),
            Doctor("Dr. Citra Dewi, M.Psi", "Konselor Pernikahan", "👩‍⚕️", 4.7, "6 Tahun", 125_000),
            Doctor("Dr. Dimas Pratama, M.Psi", "Psikolog Klinis Dewasa", "👨‍⚕️", 4.9, "10 Tahun", 200_000),
            Doctor("Dr. Eka Sari, M.Psi", "Terapis CBT", "👩‍⚕️", 4.6, "5 Tahun", 135_000),
            Doctor("Dr. Fajar Hidayat, M.Psi", "Psikolog Trauma", "👨‍⚕️", 4.8, "9 Tahun", 185_000)
        )

        val doctorAdapter = DoctorAdapter(doctors) { doctor ->
            val intent = Intent(requireContext(), PaymentActivity::class.java).apply {
                putExtra("doctor_name", doctor.name)
                putExtra("doctor_specialty", doctor.specialty)
                putExtra("doctor_emoji", doctor.emoji)
                putExtra("doctor_rating", doctor.rating)
                putExtra("doctor_experience", doctor.experience)
                putExtra("doctor_price", doctor.price)
            }
            startActivity(intent)
        }
        rvDoctors.adapter = doctorAdapter

        // Setup riwayat chat
        val rvActiveChats = view.findViewById<RecyclerView>(R.id.rv_active_chats)
        rvActiveChats.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        loadChatSessions()
    }

    private fun loadChatSessions() {
        val view = view ?: return
        val db = AppDatabase.getDatabase(requireContext())
        val rvActiveChats = view.findViewById<RecyclerView>(R.id.rv_active_chats)
        val layoutActiveChats = view.findViewById<View>(R.id.layout_active_chats)

        lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) {
                db.chatDao().getAllSessions()
            }

            if (sessions.isNotEmpty()) {
                layoutActiveChats.visibility = View.VISIBLE
                rvActiveChats.adapter = ChatSessionAdapter(sessions) { session ->
                    // Buka riwayat chat yang sudah ada
                    val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                        putExtra("doctor_name", session.doctorName)
                        putExtra("doctor_emoji", session.doctorEmoji)
                        putExtra(ChatRoomActivity.EXTRA_SESSION_ID, session.id)
                    }
                    startActivity(intent)
                }
            } else {
                layoutActiveChats.visibility = View.GONE
            }
        }
    }
}

/**
 * Data class sederhana untuk merepresentasikan dokter/psikolog.
 */
data class Doctor(
    val name: String,
    val specialty: String,
    val emoji: String,
    val rating: Double,
    val experience: String,
    val price: Int
)
