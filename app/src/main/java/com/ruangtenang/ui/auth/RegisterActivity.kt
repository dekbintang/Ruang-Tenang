package com.ruangtenang.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ruangtenang.MainActivity
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager
import com.ruangtenang.data.db.AppDatabase
import com.ruangtenang.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val db = AppDatabase.getDatabase(this)
        authRepository = AuthRepository(db.userDao())
        session = SessionManager(this)

        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etUsername = findViewById<EditText>(R.id.et_reg_username)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_reg_confirm_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_login)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = authRepository.register(email, username, password)
                result.onSuccess { newUser ->
                    session.saveSession(newUser.id, newUser.username, false)
                    Toast.makeText(this@RegisterActivity, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                }.onFailure { error ->
                    Toast.makeText(this@RegisterActivity, error.message ?: "Registrasi gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvGoLogin.setOnClickListener {
            finish() // kembali ke LoginActivity
        }
    }
}