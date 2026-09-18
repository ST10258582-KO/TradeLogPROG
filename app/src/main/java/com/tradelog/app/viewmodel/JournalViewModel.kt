package com.tradelog.app.viewmodel

import androidx.lifecycle.ViewModel
import com.tradelog.app.data.model.JournalEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JournalViewModel : ViewModel() {

    private val _entries = MutableStateFlow(sampleEntries())
    val entries: StateFlow<List<JournalEntry>> = _entries.asStateFlow()

    fun saveEntry(notes: String, checklistCompleted: Int, checklistTotal: Int) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val newEntry = JournalEntry(
            id = System.currentTimeMillis().toString(),
            date = today,
            checklistCompleted = checklistCompleted,
            checklistTotal = checklistTotal,
            notes = notes
        )
        // Replace today's entry if it already exists, otherwise prepend
        val existing = _entries.value.toMutableList()
        val todayIndex = existing.indexOfFirst { it.date == today }
        if (todayIndex >= 0) existing[todayIndex] = newEntry else existing.add(0, newEntry)
        _entries.value = existing
    }

    private fun sampleEntries() = listOf(
        JournalEntry(id="s1", date="2025-08-28", checklistCompleted=4, checklistTotal=4,
            notes="Strong London open. NAS100 broke above prior day high cleanly. Took the breakout — clean 2R. Market felt controlled all morning. Stayed patient and waited for confirmation before entry."),
        JournalEntry(id="s2", date="2025-08-27", checklistCompleted=4, checklistTotal=4,
            notes="US30 short setup during New York. Selling into resistance after a failed push higher. Took partial at 1R and let the rest run. Good discipline today — no revenge trades after the earlier choppy price action."),
        JournalEntry(id="s3", date="2025-08-26", checklistCompleted=3, checklistTotal=4,
            notes="Took a loss on EURUSD. News event caught me offside. Lesson: don't enter within 30 minutes of high-impact news. Checklist said to check news but I skimmed it — won't make that mistake again."),
        JournalEntry(id="s4", date="2025-08-25", checklistCompleted=4, checklistTotal=4,
            notes="Best day this week. NAS100 gave a textbook setup at the New York open. Held through the pullback which was tough mentally but the structure held. Ended up nearly 3R. Trust the process."),
        JournalEntry(id="s5", date="2025-08-22", checklistCompleted=4, checklistTotal=4,
            notes="GBPUSD trend short during London. Clean entry after a consolidation at resistance. Let it run to full TP. Market trending well this week — staying in line with daily bias is paying off.")
    )
}
