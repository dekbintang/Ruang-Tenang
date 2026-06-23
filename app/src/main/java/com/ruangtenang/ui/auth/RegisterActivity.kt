package com.ruangtenang.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_name)
        val etEmail = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_email)
        val etPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val tvLoginLink = findViewById<TextView>(R.id.tv_login_link)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan data pendaftaran
            val session = SessionManager(this)
            session.registeredName = name
            session.registeredEmail = email
            session.registeredPassword = password

            Toast.makeText(this, "Pendaftaran Berhasil! Silakan Login", Toast.LENGTH_LONG).show()
            finish() // Kembali ke halaman Login
        }

        btnBack.setOnClickListener {
            finish() // Kembali ke halaman Login
        }

        tvLoginLink.setOnClickListener {
            finish() // Kembali ke halaman Login
        }
    }
}
