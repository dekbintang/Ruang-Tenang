package com.ruangtenang.data.repository

import com.ruangtenang.data.db.UserDao
import com.ruangtenang.data.entity.User
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthRepository(private val userDao: UserDao) {

    private fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, saltHex: String): String {
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val spec = PBEKeySpec(password.toCharArray(), salt, 10_000, 256)
        val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    suspend fun register(email: String, username: String, password: String): Result<User> {
        if (!isValidEmail(email)) return Result.failure(Exception("Format email tidak valid"))
        if (username.length < 3) return Result.failure(Exception("Username minimal 3 karakter"))

        val existingEmail = userDao.getUserByEmail(email)
        if (existingEmail != null) return Result.failure(Exception("Email sudah terdaftar"))

        val existingUsername = userDao.getUserByUsername(username)
        if (existingUsername != null) return Result.failure(Exception("Username sudah digunakan"))

        val salt = generateSalt()
        val newUser = User(
            email = email,
            username = username,
            passwordHash = hashPassword(password, salt),
            passwordSalt = salt
        )
        val id = userDao.insertUser(newUser)
        return Result.success(newUser.copy(id = id.toInt()))
    }

    /**
     * Login menerima "identifier" yang boleh berupa EMAIL atau USERNAME,
     * supaya cocok dengan hint field di layar Login ("Username").
     */
    suspend fun login(identifier: String, password: String): User? {
        val user = if (identifier.contains("@")) {
            userDao.getUserByEmail(identifier)
        } else {
            userDao.getUserByUsername(identifier)
        } ?: return null

        if (user.isGuest) return null // guest tidak login pakai password
        val hashedInput = hashPassword(password, user.passwordSalt)
        return if (user.passwordHash == hashedInput) user else null
    }

    suspend fun loginAsGuest(): User {
        val guestUser = User(
            email = "guest_${System.currentTimeMillis()}@local",
            username = "Tamu${(1000..9999).random()}",
            passwordHash = "",
            passwordSalt = "",
            isGuest = true
        )
        val id = userDao.insertUser(guestUser)
        return guestUser.copy(id = id.toInt())
    }

    suspend fun logoutGuest(userId: Int) {
        userDao.deleteUser(userId)
    }

    // BARU — bersihkan sesi tamu yatim (app ditutup paksa tanpa logout resmi)
    suspend fun cleanupOrphanGuests(exceptUserId: Int?) {
        if (exceptUserId == null) {
            userDao.deleteAllGuests()
        }
        // Kalau ada exceptUserId (tamu yang masih aktif sesinya), biarkan —
        // hanya bersihkan saat benar-benar tidak ada sesi tamu aktif.
    }

    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)

    suspend fun updateProfile(userId: Int, username: String, age: Int?, photoUri: String?): Result<Unit> {
        if (username.isBlank()) return Result.failure(Exception("Username tidak boleh kosong"))

        val current = userDao.getUserById(userId)
            ?: return Result.failure(Exception("User tidak ditemukan"))

        // Kalau username berubah, pastikan tidak bentrok dengan user lain
        if (!username.equals(current.username, ignoreCase = true)) {
            val taken = userDao.getUserByUsername(username)
            if (taken != null && taken.id != userId) {
                return Result.failure(Exception("Username sudah digunakan"))
            }
        }

        userDao.updateUser(current.copy(username = username, age = age, photoUri = photoUri))
        return Result.success(Unit)
    }
}