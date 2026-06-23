package com.ruangtenang.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ruangtenang.MainActivity
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager
import com.ruangtenang.ui.onboarding.OnboardingActivity
import com.ruangtenang.ui.auth.AuthActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.iv_splash_logo)
        val title = findViewById<TextView>(R.id.tv_splash_title)
        val subtitle = findViewById<TextView>(R.id.tv_splash_subtitle)

        // Animasi logo
        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim)
        logo.startAnimation(logoAnim)

        // Animasi teks
        val textAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_anim)
        title.startAnimation(textAnim)
        subtitle.startAnimation(textAnim)

        // Navigasi setelah 2.5 detik
        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            val intent = when {
                !session.isOnboardingCompleted -> Intent(this, OnboardingActivity::class.java)
                !session.isLoggedIn && !session.isGuestMode -> Intent(this, AuthActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2500)
    }
}
