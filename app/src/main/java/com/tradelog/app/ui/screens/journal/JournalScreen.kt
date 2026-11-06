package com.tradelog.app.ui.screens.journal

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tradelog.app.R
import com.tradelog.app.data.model.JournalEntry
import com.tradelog.app.ui.theme.NeonGreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.JournalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "JournalScreen"

@Composable
fun JournalScreen(journalViewModel: JournalViewModel = viewModel()) {
    val entries by journalViewModel.entries.collectAsState()
    var showNewEntry by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<JournalEntry?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!showNewEntry && editingEntry == null) {
                FloatingActionButton(
                    onClick = { showNewEntry = true },
                    containerColor = Purple,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "New Entry", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(stringResource(R.string.journal_title), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }

            if (showNewEntry) {
                item {
                    EntryCard(
                        initialNotes = "",
                        initialCompleted = 0,
                        onSave = { notes, completed, total ->
                            journalViewModel.saveEntry(notes, completed, total)
                            showNewEntry = false
                            Log.i(TAG, "Journal entry saved")
                        },
                        onCancel = { showNewEntry = false }
                    )
                }
            }

            editingEntry?.let { editing ->
                item {
                    EntryCard(
                        initialNotes = editing.notes,
                        initialCompleted = editing.checklistCompleted,
                        onSave = { notes, completed, total ->
                            journalViewModel.updateEntry(editing.id, notes, completed, total)
                            editingEntry = null
                            Log.i(TAG, "Journal entry updated: ${editing.id}")
                        },
                        onCancel = { editingEntry = null }
                    )
                }
            }

            if (entries.isEmpty() && !showNewEntry && editingEntry == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No entries yet. Tap the edit button to write your first journal entry.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "Past Entries",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(entries, key = { it.id }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onEdit = { editingEntry = entry },
                        onDelete = { journalViewModel.deleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryCard(
    initialNotes: String,
    initialCompleted: Int,
    onSave: (String, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    val checklistItems = listOf("Checked daily bias", "Confirmed session", "Risk calculated", "News checked")
    val checklistState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            checklistItems.forEachIndexed { i, item -> put(item, i < initialCompleted) }
        }
    }
    var notes by remember { mutableStateOf(initialNotes) }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (initialNotes.isEmpty() && initialCompleted == 0) "Today's Entry — $today" else "Edit Entry",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            checklistItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checklistState[item] == true,
                        onCheckedChange = { checklistState[item] = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonGreen,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (checklistState[item] == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val completed = checklistState.values.count { it }
            LinearProgressIndicator(
                progress = { completed.toFloat() / checklistItems.size },
                modifier = Modifier.fillMaxWidth(),
                color = NeonGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                "$completed / ${checklistItems.size} complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Write your session review notes here...") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, cursorColor = Purple)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { onSave(notes, completed, checklistItems.size) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text(stringResource(R.string.save_entry), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    val displayDate = try {
        val parsed = LocalDate.parse(entry.date)
        parsed.format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy", Locale.ENGLISH))
    } catch (e: Exception) { entry.date }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    displayDate,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (entry.checklistCompleted == entry.checklistTotal) NeonGreen
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${entry.checklistCompleted}/${entry.checklistTotal}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Purple, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            if (entry.notes.isNotBlank()) {
                Text(
                    entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}
