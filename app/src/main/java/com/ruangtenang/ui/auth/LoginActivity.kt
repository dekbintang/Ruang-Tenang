package com.ruangtenang.ui.auth

import android.text.InputFilter
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

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var session: SessionManager

    companion object {
        private val passwordInputFilter = InputFilter { source, start, end, _, _, _ ->
            val filtered = source.subSequence(start, end).filter { it.code in 33..126 }
            if (filtered.length == end - start) null else filtered
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val db = AppDatabase.getDatabase(this)
        authRepository = AuthRepository(db.userDao())
        session = SessionManager(this)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnGuest = findViewById<Button>(R.id.btn_guest)
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_register)

        etPassword.filters = etPassword.filters + passwordInputFilter

        btnLogin.setOnClickListener {
            val identifier = etUsername.text.toString().trim()
            val password = etPassword.text.toString() // jangan trim password

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            lifecycleScope.launch {
                val user = authRepository.login(identifier, password)
                btnLogin.isEnabled = true
                if (user != null) {
                    session.saveSession(user.id, user.username, user.isGuest)
                    goToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Username/email atau password salah", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGuest.setOnClickListener {
            btnGuest.isEnabled = false
            lifecycleScope.launch {
                val guestUser = authRepository.loginAsGuest()
                session.saveSession(guestUser.id, guestUser.username, true)
                goToMain()
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}