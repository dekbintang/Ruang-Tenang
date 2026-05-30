package com.ruangtenang.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "affirmation_table")
data class Affirmation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "quote")
    val quote: String,

    @ColumnInfo(name = "author")
    val author: String? = null
)
