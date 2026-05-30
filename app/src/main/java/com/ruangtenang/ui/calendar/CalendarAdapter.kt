package com.ruangtenang.ui.calendar

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val dayNumber: Int,       // 1–31 (0 = cell kosong di awal bulan)
    val dateString: String,   // "YYYY-MM-DD"
    val moodTag: String?,     // null jika tidak ada jurnal
    val isToday: Boolean
)

class CalendarAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var days: List<CalendarDay> = emptyList()

    fun submitDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView         = itemView.findViewById(R.id.tv_day_number)
        private val ivMood: ImageView       = itemView.findViewById(R.id.iv_day_mood)
        private val frameCircle: FrameLayout = itemView.findViewById(R.id.frame_day_circle)
        private val viewDot: View           = itemView.findViewById(R.id.view_journal_dot)
        private val viewPlaceholder: View   = itemView.findViewById(R.id.view_mood_placeholder)

        fun bind(day: CalendarDay) {
            // ── Cell kosong (padding sebelum tanggal 1) ────────────────
            if (day.dayNumber == 0) {
                tvDay.text = ""
                ivMood.visibility = View.GONE
                viewDot.visibility = View.GONE
                viewPlaceholder.visibility = View.VISIBLE
                frameCircle.background = null
                itemView.isClickable = false
                return
            }

            itemView.isClickable = true
            tvDay.text = day.dayNumber.toString()

            when {
                // ── Ada jurnal → tampilkan ikon mood di dalam lingkaran berwarna ──
                day.moodTag != null -> {
                    ivMood.visibility = View.VISIBLE
                    ivMood.setImageResource(getMoodIcon(day.moodTag))
                    tvDay.visibility = View.INVISIBLE

                    // Lingkaran berwarna sesuai mood
                    val bgColor = getMoodBgColor(day.moodTag)
                    frameCircle.setBackgroundResource(R.drawable.bg_calendar_has_journal)
                    frameCircle.backgroundTintList = ColorStateList.valueOf(bgColor)

                    // Dot indikator navy di bawah
                    viewDot.visibility = View.VISIBLE
                    viewPlaceholder.visibility = View.GONE

                    itemView.setOnClickListener { onDayClick(day) }
                }

                // ── Hari ini tanpa jurnal ──────────────────────────────────────
                day.isToday -> {
                    ivMood.visibility = View.GONE
                    tvDay.visibility = View.VISIBLE
                    tvDay.setTextColor(Color.parseColor("#1E3A8A"))
                    tvDay.textSize = 13f

                    frameCircle.setBackgroundResource(R.drawable.bg_calendar_today)
                    frameCircle.backgroundTintList = null

                    viewDot.visibility = View.GONE
                    viewPlaceholder.visibility = View.VISIBLE
                    itemView.setOnClickListener(null)
                }

                // ── Hari biasa tanpa jurnal ───────────────────────────────────
                else -> {
                    ivMood.visibility = View.GONE
                    tvDay.visibility = View.VISIBLE
                    tvDay.setTextColor(Color.parseColor("#334155"))
                    tvDay.textSize = 13f

                    frameCircle.background = null
                    frameCircle.backgroundTintList = null

                    viewDot.visibility = View.GONE
                    viewPlaceholder.visibility = View.VISIBLE
                    itemView.setOnClickListener(null)
                }
            }
        }

        // Warna latar lingkaran berdasarkan mood (lembut & pastel)
        private fun getMoodBgColor(moodTag: String): Int = when (moodTag) {
            "happy"   -> Color.parseColor("#FFF3CD") // Kuning lembut
            "calm"    -> Color.parseColor("#D1F5E8") // Hijau mint
            "neutral" -> Color.parseColor("#E8ECF5") // Abu biru
            "sad"     -> Color.parseColor("#DAE8FF") // Biru muda
            "anxious" -> Color.parseColor("#FDE8D0") // Oranye muda
            "angry"   -> Color.parseColor("#FFD6D6") // Merah muda
            "shy"     -> Color.parseColor("#FCE4EC") // Pink muda
            "scared"  -> Color.parseColor("#EDE7F6") // Ungu muda
            "lazy"    -> Color.parseColor("#F0F4C3") // Kuning pucat
            else      -> Color.parseColor("#E8ECF5")
        }

        private fun getMoodIcon(moodTag: String): Int = when (moodTag) {
            "happy"   -> R.drawable.ic_mood_happy
            "calm"    -> R.drawable.ic_mood_calm
            "neutral" -> R.drawable.ic_mood_neutral
            "sad"     -> R.drawable.ic_mood_sad
            "anxious" -> R.drawable.ic_mood_anxious
            "angry"   -> R.drawable.ic_mood_angry
            "shy"     -> R.drawable.ic_mood_shy
            "scared"  -> R.drawable.ic_mood_scared
            "lazy"    -> R.drawable.ic_mood_lazy
            else      -> R.drawable.ic_mood_neutral
        }
    }

    companion object {
        // Membuat list CalendarDay untuk sebuah bulan dari Calendar + moodMap
        fun buildCalendarDays(
            calendar: Calendar,
            moodMap: Map<String, String>
        ): List<CalendarDay> {
            val days = mutableListOf<CalendarDay>()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val cal = calendar.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)

            val year  = cal.get(Calendar.YEAR)
            val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)

            // Padding di awal: Senin = 0, Minggu = 6
            var firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 2
            if (firstDayOfWeek < 0) firstDayOfWeek += 7

            repeat(firstDayOfWeek) {
                days.add(CalendarDay(0, "", null, false))
            }

            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (d in 1..maxDay) {
                val dayStr = String.format("%02d", d)
                val dateString = "$year-$month-$dayStr"
                days.add(
                    CalendarDay(
                        dayNumber  = d,
                        dateString = dateString,
                        moodTag    = moodMap[dateString],
                        isToday    = dateString == todayStr
                    )
                )
            }
            return days
        }
    }
}
