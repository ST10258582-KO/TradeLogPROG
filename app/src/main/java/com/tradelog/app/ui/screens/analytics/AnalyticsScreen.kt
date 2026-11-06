package com.tradelog.app.ui.screens.analytics

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tradelog.app.ui.theme.LossRed
import com.tradelog.app.ui.theme.NeonGreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.SettingsViewModel
import com.tradelog.app.viewmodel.TradeViewModel

private const val TAG = "AnalyticsScreen"

// Analytics screen — computes breakdown from actual saved trades (FR-07).
@Composable
fun AnalyticsScreen(
    tradeViewModel: TradeViewModel,
    analyticsViewModel: com.tradelog.app.viewmodel.AnalyticsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val trades by tradeViewModel.trades.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val isLoading by tradeViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedInstrument by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("Overview", "By Strategy", "By Session")

    LaunchedEffect(Unit) {
        Log.d(TAG, "AnalyticsScreen launched")
        tradeViewModel.loadTrades()
    }

    // Apply instrument filter
    val filteredTrades = if (selectedInstrument == null) trades
    else trades.filter { it.instrument == selectedInstrument }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Instrument filter chips
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedInstrument == null,
                    onClick = { selectedInstrument = null },
                    label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple, selectedLabelColor = Color.White
                    )
                )
            }
            items(settings.instruments) { inst ->
                FilterChip(
                    selected = selectedInstrument == inst,
                    onClick = { selectedInstrument = if (selectedInstrument == inst) null else inst },
                    label = { Text(inst, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple, selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Purple
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple)
            }
        } else if (filteredTrades.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No trades logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            when (selectedTab) {
                0 -> OverviewTab(filteredTrades)
                1 -> StrategyTab(filteredTrades, settings.strategies)
                2 -> SessionTab(filteredTrades, settings.sessions)
            }
        }
    }
}

// ── Overview ──────────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(trades: List<Trade>) {
    val closed = trades.filter { it.pnl != null }
    val wins = closed.count { (it.pnl ?: 0.0) > 0 }
    val winRate = if (closed.isEmpty()) 0.0 else wins.toDouble() / closed.size
    val netPnl = closed.sumOf { it.pnl ?: 0.0 }
    val avgR = if (closed.isEmpty()) 0.0 else closed.mapNotNull { it.rMultiple }.let {
        if (it.isEmpty()) 0.0 else it.average()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatCard("Total Trades", trades.size.toString()) }
        item { StatCard("Closed Trades", closed.size.toString()) }
        item { StatCard("Win Rate", if (closed.isEmpty()) "--" else String.format("%.1f%%", winRate * 100)) }
        item {
            StatCard(
                "Net P&L",
                if (closed.isEmpty()) "--" else (if (netPnl >= 0) "+$" else "-$") + String.format("%.2f", Math.abs(netPnl)),
                valueColor = if (netPnl >= 0) NeonGreen else LossRed
            )
        }
        item { StatCard("Average R", if (closed.isEmpty()) "--" else String.format("%.2fR", avgR)) }
        item { StatCard("Open Trades", trades.count { it.pnl == null }.toString()) }
    }
}

// ── By Strategy ───────────────────────────────────────────────────────────────

@Composable
private fun StrategyTab(trades: List<Trade>, strategies: List<String>) {
    // Build stats for each strategy that has at least one trade
    val rows = strategies.mapNotNull { strat ->
        val stratTrades = trades.filter { it.tags.contains(strat) }
        if (stratTrades.isEmpty()) return@mapNotNull null
        val closed = stratTrades.filter { it.pnl != null }
        val wins = closed.count { (it.pnl ?: 0.0) > 0 }
        val wr = if (closed.isEmpty()) 0.0 else wins.toDouble() / closed.size
        val pnl = closed.sumOf { it.pnl ?: 0.0 }
        val avgR = closed.mapNotNull { it.rMultiple }.let { if (it.isEmpty()) 0.0 else it.average() }
        StratRow(strat, stratTrades.size, wr, pnl, avgR)
    }

    if (rows.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No strategy data yet. Tag your trades when logging.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows) { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Purple.copy(alpha = 0.2f)) {
                            Text(row.name, color = Purple, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Text("${row.count} trades", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row {
                        Text(String.format("WR: %.1f%%", row.winRate * 100), style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.weight(1f))
                        Text(String.format("Avg R: %.2f", row.avgR), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(
                            (if (row.netPnl >= 0) "+$" else "-$") + String.format("%.0f", Math.abs(row.netPnl)),
                            color = if (row.netPnl >= 0) NeonGreen else LossRed,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private data class StratRow(val name: String, val count: Int, val winRate: Double, val netPnl: Double, val avgR: Double)

// ── By Session ────────────────────────────────────────────────────────────────

@Composable
private fun SessionTab(trades: List<Trade>, sessions: List<String>) {
    val rows = sessions.mapNotNull { session ->
        val sessionTrades = trades.filter { it.session?.name == session }
        if (sessionTrades.isEmpty()) return@mapNotNull null
        val closed = sessionTrades.filter { it.pnl != null }
        val wins = closed.count { (it.pnl ?: 0.0) > 0 }
        val wr = if (closed.isEmpty()) 0.0 else wins.toDouble() / closed.size
        val pnl = closed.sumOf { it.pnl ?: 0.0 }
        val avgR = closed.mapNotNull { it.rMultiple }.let { if (it.isEmpty()) 0.0 else it.average() }
        StratRow(session, sessionTrades.size, wr, pnl, avgR)
    }

    if (rows.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No session data yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows) { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(row.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row {
                        Text(String.format("WR: %.1f%%", row.winRate * 100), style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.weight(1f))
                        Text(String.format("Avg R: %.2f", row.avgR), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(
                            (if (row.netPnl >= 0) "+$" else "-$") + String.format("%.0f", Math.abs(row.netPnl)),
                            color = if (row.netPnl >= 0) NeonGreen else LossRed,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text("${row.count} trades", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Shared ────────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(label: String, value: String, valueColor: Color = Color.White) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor, fontWeight = FontWeight.Bold)
        }
    }
}
