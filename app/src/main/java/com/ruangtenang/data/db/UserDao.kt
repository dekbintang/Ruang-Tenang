package com.ruangtenang.data.db

import androidx.room.*
import com.ruangtenang.data.entity.User

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM user_table WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("DELETE FROM user_table WHERE is_guest = 1")
    suspend fun deleteAllGuests()
}