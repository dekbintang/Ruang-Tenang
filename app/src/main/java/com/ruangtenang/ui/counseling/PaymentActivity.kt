package com.ruangtenang.ui.counseling

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ruangtenang.R
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val doctorName = intent.getStringExtra("doctor_name") ?: "Dokter"
        val doctorSpecialty = intent.getStringExtra("doctor_specialty") ?: ""
        val doctorEmoji = intent.getStringExtra("doctor_emoji") ?: "👨‍⚕️"
        val doctorRating = intent.getDoubleExtra("doctor_rating", 0.0)
        val doctorExperience = intent.getStringExtra("doctor_experience") ?: ""
        val doctorPrice = intent.getIntExtra("doctor_price", 0)

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        // Set info dokter
        findViewById<TextView>(R.id.tv_payment_doctor_emoji).text = doctorEmoji
        findViewById<TextView>(R.id.tv_payment_doctor_name).text = doctorName
        findViewById<TextView>(R.id.tv_payment_doctor_specialty).text = doctorSpecialty
        findViewById<TextView>(R.id.tv_payment_doctor_rating).text = "⭐ $doctorRating • $doctorExperience"

        // Set harga
        val platformFee = 5000
        val total = doctorPrice + platformFee
        findViewById<TextView>(R.id.tv_payment_session_price).text = currencyFormat.format(doctorPrice)
        findViewById<TextView>(R.id.tv_payment_total).text = currencyFormat.format(total)

        // Tombol kembali
        findViewById<ImageButton>(R.id.btn_back_payment).setOnClickListener {
            finish()
        }

        // Tombol bayar
        findViewById<MaterialButton>(R.id.btn_pay_now).setOnClickListener {
            simulatePayment(doctorName, doctorEmoji)
        }
    }

    private fun simulatePayment(doctorName: String, doctorEmoji: String) {
        val btnPay = findViewById<MaterialButton>(R.id.btn_pay_now)
        btnPay.isEnabled = false
        btnPay.text = "Memproses..."

        // Simulasi proses pembayaran selama 2 detik
        Handler(Looper.getMainLooper()).postDelayed({
            // Tampilkan dialog sukses
            AlertDialog.Builder(this)
                .setTitle("✅ Pembayaran Berhasil!")
                .setMessage("Pembayaran kamu telah dikonfirmasi.\nSekarang kamu bisa mulai konseling dengan $doctorName.")
                .setPositiveButton("Mulai Chat") { _, _ ->
                    // Buka halaman chat
                    val intent = Intent(this, ChatRoomActivity::class.java).apply {
                        putExtra("doctor_name", doctorName)
                        putExtra("doctor_emoji", doctorEmoji)
                    }
                    startActivity(intent)
                    finish()
                }
                .setCancelable(false)
                .show()
        }, 2000)
    }
}
