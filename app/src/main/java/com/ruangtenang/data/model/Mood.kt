package com.ruangtenang.data.model

import android.graphics.Color

data class Mood(
    val id: String,
    val label: String,
    val emoji: String,
    val colorHex: String
) {
    companion object {
        val MOODS: List<Mood> = listOf(
            Mood("happy", "Senang", "😄", "#FFD166"),
            Mood("calm", "Tenang", "🙂", "#55EFC4"),
            Mood("neutral", "Netral", "😐", "#B2BEC3"),
            Mood("sad", "Sedih", "😢", "#74B9FF"),
            Mood("anxious", "Cemas", "😟", "#FFA07A"),
            Mood("angry", "Marah", "😠", "#D90429"),
            Mood("shy", "Malu", "☺️", "#F72585"),
            Mood("scared", "Takut", "😨", "#7209B7"),
            Mood("lazy", "Malas", "😴", "#9C6644"),
        )

        private val byId: Map<String, Mood> = MOODS.associateBy { it.id }

        fun fromId(id: String): Mood =
            byId[id] ?: Mood(id, id.replaceFirstChar { it.uppercase() }, "❓", "#B2BEC3")

        fun getBackgroundColor(moodId: String): Int {
            val mood = fromId(moodId)
            return Color.parseColor(mood.colorHex)
        }
    }
}