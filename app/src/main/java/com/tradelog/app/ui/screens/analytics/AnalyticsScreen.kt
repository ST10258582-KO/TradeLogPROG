package com.tradelog.app.ui.screens.analytics

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradelog.app.data.remote.SessionDetail
import com.tradelog.app.data.remote.TagStat
import com.tradelog.app.ui.theme.LossRed
import com.tradelog.app.ui.theme.NeonGreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.AnalyticsViewModel
import com.tradelog.app.viewmodel.TradeViewModel

private const val TAG = "AnalyticsScreen"

// Analytics screen — breakdown by strategy tag, session, and summary overview (FR-07).
@Composable
fun AnalyticsScreen(
    tradeViewModel: TradeViewModel,
    analyticsViewModel: AnalyticsViewModel
) {
    val summary by tradeViewModel.summary.collectAsState()
    val sessionStats by analyticsViewModel.sessionStats.collectAsState()
    val tagStats by analyticsViewModel.tagStats.collectAsState()
    val isLoading by analyticsViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "By Strategy", "By Session")

    LaunchedEffect(Unit) {
        Log.d(TAG, "AnalyticsScreen launched")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            "Analytics",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Purple
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index; Log.d(TAG, "Tab selected: $title") },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple)
            }
        } else {
            when (selectedTab) {
                0 -> OverviewTab(summary = summary)
                1 -> TagBreakdownTab(tagStats?.tags ?: emptyList())
                2 -> SessionBreakdownTab(sessionStats)
            }
        }
    }
}

@Composable
private fun OverviewTab(summary: com.tradelog.app.data.remote.AnalyticsSummary?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryStatCard("Win Rate", summary?.let { String.format("%.1f%%", it.winRate * 100) } ?: "--")
        }
        item {
            SummaryStatCard("Net P&L", summary?.let { (if (it.netPnl >= 0) "+$" else "-$") + String.format("%.2f", Math.abs(it.netPnl)) } ?: "--",
                valueColor = if ((summary?.netPnl ?: 0.0) >= 0) NeonGreen else LossRed)
        }
        item {
            SummaryStatCard("Average R", summary?.let { String.format("%.2fR", it.avgR) } ?: "--")
        }
        item {
            SummaryStatCard("Total Trades", summary?.tradeCount?.toString() ?: "--")
        }
    }
}

@Composable
private fun SummaryStatCard(label: String, value: String, valueColor: Color = Color.White) {
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

@Composable
private fun TagBreakdownTab(tags: List<TagStat>) {
    if (tags.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tag data yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row {
                        Surface(shape = RoundedCornerShape(20.dp), color = Purple.copy(alpha = 0.2f)) {
                            Text(tag.name, color = Purple, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Text(String.format("%.1f%%", tag.winRate * 100), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text("Avg R: ${String.format("%.2f", tag.avgR)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        val pnl = tag.netPnl
                        Text(
                            (if (pnl >= 0) "+$" else "-$") + String.format("%.0f", Math.abs(pnl)),
                            color = if (pnl >= 0) NeonGreen else LossRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionBreakdownTab(sessionStats: com.tradelog.app.data.remote.SessionStats?) {
    if (sessionStats == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No session data yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SessionCard("London", sessionStats.london) }
        item { SessionCard("New York", sessionStats.newYork) }
        item { SessionCard("Asia", sessionStats.asia) }
    }
}

@Composable
private fun SessionCard(name: String, stats: SessionDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row {
                StatPill("WR", String.format("%.0f%%", stats.winRate * 100))
                Spacer(Modifier.width(8.dp))
                StatPill("Avg R", String.format("%.2f", stats.avgR))
                Spacer(Modifier.width(8.dp))
                StatPill("Trades", stats.tradeCount.toString())
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
