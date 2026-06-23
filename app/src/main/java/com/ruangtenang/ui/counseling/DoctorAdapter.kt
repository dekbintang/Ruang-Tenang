package com.ruangtenang.ui.counseling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ruangtenang.R
import java.text.NumberFormat
import java.util.Locale

class DoctorAdapter(
    private val doctors: List<Doctor>,
    private val onItemClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tv_doctor_emoji)
        val tvName: TextView = itemView.findViewById(R.id.tv_doctor_name)
        val tvSpecialty: TextView = itemView.findViewById(R.id.tv_doctor_specialty)
        val tvRating: TextView = itemView.findViewById(R.id.tv_doctor_rating)
        val tvExperience: TextView = itemView.findViewById(R.id.tv_doctor_experience)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_doctor_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]

        holder.tvEmoji.text = doctor.emoji
        holder.tvName.text = doctor.name
        holder.tvSpecialty.text = doctor.specialty
        holder.tvRating.text = "⭐ ${doctor.rating}"
        holder.tvExperience.text = doctor.experience
        holder.tvPrice.text = currencyFormat.format(doctor.price)

        holder.itemView.setOnClickListener {
            onItemClick(doctor)
        }
    }

    override fun getItemCount(): Int = doctors.size
}
