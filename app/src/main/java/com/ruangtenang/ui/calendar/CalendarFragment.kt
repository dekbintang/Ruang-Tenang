package com.ruangtenang.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ruangtenang.R

class CalendarFragment : Fragment() {

    private lateinit var viewModel: CalendarViewModel
    private lateinit var adapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        setupCalendarGrid(view)
        setupNavigation(view)
        observeData(view)
        observeSelectedJournal()
    }

    // ── Setup Grid 7 kolom untuk kalender ────────────────────────────────
    private fun setupCalendarGrid(view: View) {
        adapter = CalendarAdapter { clickedDay ->
            // Dipanggil saat tanggal dengan jurnal diklik
            viewModel.onDateClicked(clickedDay.dateString)
        }

        val rvCalendar = view.findViewById<RecyclerView>(R.id.rv_calendar)
        rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        rvCalendar.adapter = adapter
    }

    // ── Tombol < dan > untuk navigasi bulan ──────────────────────────────
    private fun setupNavigation(view: View) {
        view.findViewById<ImageButton>(R.id.btn_prev_month).setOnClickListener {
            viewModel.goToPreviousMonth()
        }
        view.findViewById<ImageButton>(R.id.btn_next_month).setOnClickListener {
            viewModel.goToNextMonth()
        }
    }

    // ── Observasi data dari ViewModel ─────────────────────────────────────
    private fun observeData(view: View) {
        val tvMonthYear = view.findViewById<TextView>(R.id.tv_month_year)

        // Update header bulan & tahun
        viewModel.currentCalendar.observe(viewLifecycleOwner) { cal ->
            val monthNames = arrayOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
            val month = monthNames[cal.get(java.util.Calendar.MONTH)]
            val year  = cal.get(java.util.Calendar.YEAR)
            tvMonthYear.text = "$month $year"
        }

        // Update grid kalender saat mood map atau bulan berubah
        viewModel.monthMoodMap.observe(viewLifecycleOwner) { moodMap ->
            val cal = viewModel.currentCalendar.value ?: return@observe
            val days = CalendarAdapter.buildCalendarDays(cal, moodMap)
            adapter.submitDays(days)
        }
    }

    // ── Tampilkan pop-up preview jurnal saat tanggal diklik ───────────────
    private fun observeSelectedJournal() {
        viewModel.selectedJournal.observe(viewLifecycleOwner) { journal ->
            journal ?: return@observe

            val bottomSheet = BottomSheetDialog(requireContext())
            val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_journal_preview, null)

            val moodLabel = when (journal.moodTag) {
                "happy"   -> "Senang"
                "calm"    -> "Tenang"
                "neutral" -> "Biasa"
                "sad"     -> "Sedih"
                "anxious" -> "Cemas"
                "angry"   -> "Marah"
                "shy"     -> "Malu"
                "scared"  -> "Takut"
                "lazy"    -> "Malas"
                else      -> "Jurnal"
            }

            val moodIcon = when (journal.moodTag) {
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

            sheetView.findViewById<TextView>(R.id.tv_sheet_date).text  = journal.dateString
            sheetView.findViewById<android.widget.ImageView>(R.id.iv_sheet_mood).setImageResource(moodIcon)
            sheetView.findViewById<TextView>(R.id.tv_sheet_mood).text  = moodLabel
            sheetView.findViewById<TextView>(R.id.tv_sheet_title).text = journal.title
            sheetView.findViewById<TextView>(R.id.tv_sheet_content).text = journal.content

            bottomSheet.setContentView(sheetView)
            bottomSheet.show()

            // Reset selected journal setelah ditampilkan
            viewModel.selectedJournal.value = null
        }
    }
}
