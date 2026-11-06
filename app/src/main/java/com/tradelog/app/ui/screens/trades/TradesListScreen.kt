package com.tradelog.app.ui.screens.trades

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradelog.app.R
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.model.TradeDirection
import com.tradelog.app.ui.theme.LossRed
import com.tradelog.app.ui.theme.NeonGreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.TradeViewModel

private const val TAG = "TradesListScreen"

// Trades list screen — shows all logged trades with filter chips and FAB to add new trade (FR-05).
@Composable
fun TradesListScreen(
    tradeViewModel: TradeViewModel,
    onTradeClick: (String) -> Unit,
    onAddTrade: () -> Unit
) {
    val trades by tradeViewModel.trades.collectAsState()
    val isLoading by tradeViewModel.isLoading.collectAsState()
    var selectedSession by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d(TAG, "TradesListScreen launched")
        tradeViewModel.loadTrades()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d(TAG, "FAB tapped — navigating to TradeEntry")
                    onAddTrade()
                },
                containerColor = Purple,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trade", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.trades_title), style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.weight(1f))
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))

            // Session filter chips (FR-05)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null, "LONDON", "NEW_YORK", "ASIA").forEach { session ->
                    FilterChip(
                        selected = selectedSession == session,
                        onClick = {
                            selectedSession = session
                            Log.d(TAG, "Session filter changed: $session")
                            tradeViewModel.loadTrades(session = session)
                        },
                        label = { Text(session ?: "All", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            } else if (trades.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No trades found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trades, key = { it.id }) { trade ->
                        TradeListItem(trade = trade, onClick = { onTradeClick(trade.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeListItem(trade: Trade, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Colour-coded direction indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        if (trade.direction == TradeDirection.BUY) Purple else LossRed,
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(trade.instrument, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    "${trade.direction.name} • ${trade.session?.name ?: "Unknown"} • ${trade.entryTime.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val pnl = trade.pnl
                Text(
                    if (pnl != null) (if (pnl >= 0) "+$" else "-$") + String.format("%.0f", Math.abs(pnl)) else "Open",
                    color = when { pnl == null -> Color.White; pnl >= 0 -> NeonGreen; else -> LossRed },
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                trade.rMultiple?.let {
                    Text(String.format("%.1fR", it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
