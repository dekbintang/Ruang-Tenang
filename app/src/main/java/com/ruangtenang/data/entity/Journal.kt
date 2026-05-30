package com.ruangtenang.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_table")
data class Journal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    // Nilai: "happy" | "calm" | "neutral" | "sad" | "anxious"
    @ColumnInfo(name = "mood_tag")
    val moodTag: String,

    // Format: "YYYY-MM-DD" — digunakan untuk query kalender
    @ColumnInfo(name = "date_string")
    val dateString: String,

    // Unix timestamp (ms) — digunakan untuk sorting terbaru
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
