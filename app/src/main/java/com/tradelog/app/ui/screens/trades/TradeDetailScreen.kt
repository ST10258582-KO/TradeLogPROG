package com.tradelog.app.ui.screens.trades

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradelog.app.data.model.TradeDirection
import com.tradelog.app.data.model.TradeStatus
import com.tradelog.app.ui.theme.LossRed
import com.tradelog.app.ui.theme.NeonGreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.TradeViewModel

private const val TAG = "TradeDetailScreen"

// Trade detail screen — shows all fields for a trade with edit and delete actions (FR-06).
@Composable
fun TradeDetailScreen(
    tradeId: String,
    tradeViewModel: TradeViewModel,
    onBack: () -> Unit
) {
    val trade by tradeViewModel.selectedTrade.collectAsState()
    val isLoading by tradeViewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tradeId) {
        Log.d(TAG, "TradeDetailScreen — loading trade: $tradeId")
        tradeViewModel.loadTrade(tradeId)
    }

    // Confirm delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trade", color = Color.White) },
            text = { Text("This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    Log.d(TAG, "Deleting trade: $tradeId")
                    tradeViewModel.deleteTrade(tradeId) { onBack() }
                    showDeleteDialog = false
                }) { Text("Delete", color = LossRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Trade Detail", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LossRed)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        if (isLoading || trade == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple)
            }
        } else {
            val t = trade!!
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Instrument + direction header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.instrument, style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (t.direction == TradeDirection.BUY) Purple.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f)
                    ) {
                        Text(
                            t.direction.name,
                            color = if (t.direction == TradeDirection.BUY) Purple else LossRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // P&L + R-Multiple
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("P&L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val pnl = t.pnl
                            Text(
                                if (pnl != null) (if (pnl >= 0) "+$" else "-$") + String.format("%.2f", Math.abs(pnl)) else "Open",
                                style = MaterialTheme.typography.headlineMedium,
                                color = when { pnl == null -> Color.White; pnl >= 0 -> NeonGreen; else -> LossRed },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("R-Multiple", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                t.rMultiple?.let { String.format("%.2fR", it) } ?: "--",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Price details
                DetailCard {
                    DetailRow("Session", t.session?.name ?: "Unknown")
                    DetailRow("Status", t.status.name)
                    DetailRow("Entry Price", t.entryPrice.toString())
                    DetailRow("Stop Loss", t.stopLoss.toString())
                    DetailRow("Take Profit", t.takeProfit.toString())
                    t.exitPrice?.let { DetailRow("Exit Price", it.toString()) }
                    DetailRow("Quantity", t.quantity.toString())
                    DetailRow("Entry Time", t.entryTime.take(16).replace("T", " "))
                    t.exitTime?.let { DetailRow("Exit Time", it.take(16).replace("T", " ")) }
                }

                // Tags
                if (t.tags.isNotEmpty()) {
                    Text("Strategy Tags", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        t.tags.forEach { tag ->
                            Surface(shape = RoundedCornerShape(20.dp), color = Purple.copy(alpha = 0.2f)) {
                                Text(tag, color = Purple, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Notes
                t.notes?.let { note ->
                    Text("Notes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(note, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
    }
}
