package com.ruangtenang.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ruangtenang.MainActivity
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnSignUp = findViewById<View>(R.id.btn_signup)
        val btnGuest = findViewById<View>(R.id.btn_guest)
        
        val etEmail = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_email)
        val etPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_password)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val session = SessionManager(this)
            val regEmail = session.registeredEmail
            val regPassword = session.registeredPassword

            if (regEmail == null) {
                Toast.makeText(this, "Akun belum terdaftar, silakan Sign Up", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email == regEmail && password == regPassword) {
                // Login Sukses
                session.isLoggedIn = true
                session.isGuestMode = false
                
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
            }
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnGuest.setOnClickListener {
            val session = SessionManager(this)
            session.isLoggedIn = false
            session.isGuestMode = true
            
            Toast.makeText(this, "Masuk sebagai Guest", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
