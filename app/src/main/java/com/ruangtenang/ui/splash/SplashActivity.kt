package com.ruangtenang.ui.splash

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
import com.ruangtenang.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.iv_splash_logo)
        val title = findViewById<TextView>(R.id.tv_splash_title)
        val subtitle = findViewById<TextView>(R.id.tv_splash_subtitle)

        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim)
        logo.startAnimation(logoAnim)

        val textAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_anim)
        title.startAnimation(textAnim)
        subtitle.startAnimation(textAnim)

        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (session.isLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 2500)
    }
}