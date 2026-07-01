package com.ruangtenang.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "is_guest")
    val isGuest: Boolean = false,

    @ColumnInfo(name = "age")
    val age: Int? = null,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null
)