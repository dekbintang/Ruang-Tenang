package com.ruangtenang.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan sesi konseling (riwayat chat).
 * Setiap kali pengguna membayar dan memulai chat baru, satu record dibuat.
 */
@Entity(tableName = "chat_session_table")
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "doctor_name")
    val doctorName: String,

    @ColumnInfo(name = "doctor_emoji")
    val doctorEmoji: String,

    @ColumnInfo(name = "last_message")
    val lastMessage: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
