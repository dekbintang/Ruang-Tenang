package com.ruangtenang.data.repository

import com.ruangtenang.data.db.UserDao
import com.ruangtenang.data.entity.User
import java.security.MessageDigest

class AuthRepository(private val userDao: UserDao) {

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun register(username: String, password: String): User? {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) return null

        val newUser = User(
            username = username,
            passwordHash = hashPassword(password)
        )
        val id = userDao.insertUser(newUser)
        return newUser.copy(id = id.toInt())
    }

    suspend fun login(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username) ?: return null
        return if (user.passwordHash == hashPassword(password)) user else null
    }

    suspend fun loginAsGuest(): User {
        val guestUser = User(
            username = "guest_${System.currentTimeMillis()}",
            passwordHash = "",
            isGuest = true
        )
        val id = userDao.insertUser(guestUser)
        return guestUser.copy(id = id.toInt())
    }

    suspend fun logoutGuest(userId: Int) {
        userDao.deleteUser(userId)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateProfile(userId: Int, age: Int?, photoUri: String?): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        val updated = user.copy(age = age, photoUri = photoUri)
        userDao.updateUser(updated)
        return true
    }
}