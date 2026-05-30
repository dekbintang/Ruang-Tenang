package com.ruangtenang.ui

import android.view.animation.DecelerateInterpolator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.ruangtenang.R

class StreakBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_STREAK = "streak_count"

        fun newInstance(streakCount: Int): StreakBottomSheet {
            return StreakBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_STREAK, streakCount)
                }
            }
        }
    }

    private var flameContainer: View? = null
    private var tvSheetFlame: TextView? = null
    private var tvStreakTitle: TextView? = null
    private var tvStreakMessage: TextView? = null
    private var btnStreakClose: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_streak, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        flameContainer  = view.findViewById(R.id.flame_container)
        tvSheetFlame    = view.findViewById(R.id.tv_sheet_flame)
        tvStreakTitle   = view.findViewById(R.id.tv_streak_title)
        tvStreakMessage = view.findViewById(R.id.tv_streak_message)
        btnStreakClose  = view.findViewById(R.id.btn_streak_close)

        val streak = arguments?.getInt(ARG_STREAK, 1) ?: 1

        applyStreakLevel(streak)

        btnStreakClose?.setOnClickListener { dismiss() }
    }

    private fun applyStreakLevel(streak: Int) {
        val level = when {
            streak >= 14 -> StreakLevel(
                bgRes      = R.drawable.bg_flame_level4,
                ringColor  = "#A78BFA",
                animRes    = R.anim.flame_flicker_fast,
                emoji      = "🏆",
                title      = "Streak $streak Hari — Legenda Api!",
                message    = "Kamu luar biasa! Tidak ada yang bisa memadamkan apimu."
            )
            streak >= 7 -> StreakLevel(
                bgRes      = R.drawable.bg_flame_level3,
                ringColor  = "#FB7185",
                animRes    = R.anim.flame_flicker_fast,
                emoji      = "🌟",
                title      = "Streak $streak Hari — Api Membara!",
                message    = "Energimu meledak! Terus jaga momentumnya."
            )
            streak >= 4 -> StreakLevel(
                bgRes      = R.drawable.bg_flame_level2,
                ringColor  = "#F59E0B",
                animRes    = R.anim.flame_flicker_medium,
                emoji      = "🔥",
                title      = "Streak $streak Hari — Api Menyala!",
                message    = "Apinya membesar! Jangan sampai padam."
            )
            else -> StreakLevel(
                bgRes      = R.drawable.bg_flame_level1,
                ringColor  = "#FB923C",
                animRes    = R.anim.flame_flicker_slow,
                emoji      = "🔥",
                title      = "Streak $streak Hari — Percikan Awal!",
                message    = "Perjalanan dimulai dari percikan kecil. Teruskan!"
            )
        }

        flameContainer?.setBackgroundResource(level.bgRes)

        tvSheetFlame?.let { tv ->
            val anim = AnimationUtils.loadAnimation(requireContext(), level.animRes)
            tv.startAnimation(anim)
            tv.text = level.emoji
        }

        tvStreakTitle?.text   = level.title
        tvStreakMessage?.text = level.message

        val color = Color.parseColor(level.ringColor)
        btnStreakClose?.backgroundTintList = ColorStateList.valueOf(color)

        applyPulseRing(streak, level.ringColor)
    }

    private fun applyPulseRing(streak: Int, hexColor: String) {
        val container = flameContainer ?: return

        val ringCount = when {
            streak >= 14 -> 3
            streak >= 7  -> 2
            else         -> 1
        }

        container.animate().cancel()
        container.scaleX = 1f
        container.scaleY = 1f
        container.alpha  = 1f

        repeat(ringCount) { i ->
            val pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.4f)
            val pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.4f)
            val pvhAlpha  = PropertyValuesHolder.ofFloat("alpha",  0.7f, 0f)

            ObjectAnimator.ofPropertyValuesHolder(container, pvhScaleX, pvhScaleY, pvhAlpha)
                .apply {
                    duration     = 1500
                    startDelay   = (i * 500L)
                    repeatCount  = ValueAnimator.INFINITE
                    interpolator = DecelerateInterpolator()
                    start()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        flameContainer?.animate()?.cancel()
        tvSheetFlame?.clearAnimation()
        flameContainer  = null
        tvSheetFlame    = null
        tvStreakTitle   = null
        tvStreakMessage = null
        btnStreakClose  = null
    }

    private data class StreakLevel(
        val bgRes: Int,
        val ringColor: String,
        val animRes: Int,
        val emoji: String,
        val title: String,
        val message: String
    )
}