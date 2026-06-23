package com.ruangtenang.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ruangtenang.R
import com.ruangtenang.data.SessionManager
import com.ruangtenang.ui.auth.AuthActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btn_next)

        val layouts = listOf(
            R.layout.item_onboarding_1,
            R.layout.item_onboarding_2
        )

        viewPager.adapter = OnboardingAdapter(layouts)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == layouts.size - 1) {
                    btnNext.text = "Get Started"
                } else {
                    btnNext.text = "Continue"
                }
            }
        })

        btnNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < layouts.size - 1) {
                viewPager.currentItem = currentItem + 1
            } else {
                // Selesai onboarding
                val session = SessionManager(this)
                session.isOnboardingCompleted = true
                
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }
    }
}

class OnboardingAdapter(private val layouts: List<Int>) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // No specific binding needed for page 1-3
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int {
        return layouts[position]
    }
}
