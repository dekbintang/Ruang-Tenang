package com.ruangtenang.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan setiap pesan chat dalam sebuah sesi.
 */
@Entity(tableName = "chat_message_table")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "session_id")
    val sessionId: Int,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "is_from_user")
    val isFromUser: Boolean,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
