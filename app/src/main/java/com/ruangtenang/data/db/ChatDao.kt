package com.ruangtenang.data.db

import androidx.room.*
import com.ruangtenang.data.entity.ChatMessageEntity
import com.ruangtenang.data.entity.ChatSession

@Dao
interface ChatDao {

    // ── SESSION ──────────────────────────────────────────────
    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Query("SELECT * FROM chat_session_table ORDER BY updated_at DESC")
    suspend fun getAllSessions(): List<ChatSession>

    @Query("UPDATE chat_session_table SET last_message = :lastMessage, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionLastMessage(sessionId: Int, lastMessage: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_session_table WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Int)

    // ── MESSAGES ─────────────────────────────────────────────
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_message_table WHERE session_id = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: Int): List<ChatMessageEntity>

    @Query("DELETE FROM chat_message_table WHERE session_id = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Int)
}
