package com.tradelog.app.ui.screens.dashboard

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.model.TradeDirection
import com.tradelog.app.data.remote.AnalyticsSummary
import com.tradelog.app.data.remote.EquityCurvePoint
import com.tradelog.app.ui.theme.LossRed
import com.tradelog.app.ui.theme.NeonGreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.TradeViewModel

private const val TAG = "DashboardScreen"

// Dashboard home screen — displays summary statistics, equity curve, and recent trades (FR-07).
@Composable
fun DashboardScreen(
    tradeViewModel: TradeViewModel,
    onTradeClick: (String) -> Unit
) {
    val trades by tradeViewModel.trades.collectAsState()
    val summary by tradeViewModel.summary.collectAsState()
    val equityCurve by tradeViewModel.equityCurve.collectAsState()
    val isLoading by tradeViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        Log.d(TAG, "DashboardScreen launched")
        tradeViewModel.loadDashboard()
        tradeViewModel.loadTrades()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("TradeLog", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        item { SummaryRow(summary = summary) }
        item { EquityCurveCard(points = equityCurve) }
        item {
            Text(
                "Recent Trades",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            }
        } else if (trades.isEmpty()) {
            item {
                Text(
                    "No trades logged yet. Tap + to add your first trade.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(trades.take(5)) { trade ->
                TradeRowCard(trade = trade, onClick = { onTradeClick(trade.id) })
            }
        }
    }
}

@Composable
private fun SummaryRow(summary: AnalyticsSummary?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("WIN RATE", summary?.let { "${(it.winRate * 100).toInt()}%" } ?: "--", modifier = Modifier.weight(1f))
        StatCard(
            "NET P&L",
            summary?.let { formatPnl(it.netPnl) } ?: "--",
            valueColor = when {
                summary == null -> Color.White
                summary.netPnl >= 0 -> NeonGreen
                else -> LossRed
            },
            modifier = Modifier.weight(1f)
        )
        StatCard("AVG R", summary?.let { String.format("%.2fR", it.avgR) } ?: "--", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color = Color.White, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor, fontWeight = FontWeight.Bold)
        }
    }
}

// Equity curve rendered with Jetpack Compose Canvas API (FR-07, innovative feature).
// Reference: Google LLC, "Canvas in Compose" Android Developers (2024)
@Composable
private fun EquityCurveCard(points: List<EquityCurvePoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Equity Curve", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(12.dp))
            if (points.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("No data yet", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    val minPnl = points.minOf { it.cumulativePnl }
                    val maxPnl = points.maxOf { it.cumulativePnl }
                    val range = (maxPnl - minPnl).coerceAtLeast(1.0)
                    val xStep = size.width / (points.size - 1).coerceAtLeast(1)
                    val path = Path()
                    points.forEachIndexed { i, point ->
                        val x = i * xStep
                        val y = size.height - ((point.cumulativePnl - minPnl) / range * size.height).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    val lineColor = if (points.last().cumulativePnl >= 0) NeonGreen else LossRed
                    drawPath(path, color = lineColor, style = Stroke(width = 2.5f))
                    val zeroY = size.height - ((-minPnl) / range * size.height).toFloat()
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1f)
                }
            }
        }
    }
}

@Composable
private fun TradeRowCard(trade: Trade, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (trade.direction == TradeDirection.BUY) Purple.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f)
            ) {
                Text(
                    trade.direction.name,
                    color = if (trade.direction == TradeDirection.BUY) Purple else LossRed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(trade.instrument, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(trade.entryTime.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                val pnl = trade.pnl
                Text(
                    if (pnl != null) formatPnl(pnl) else "Open",
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

private fun formatPnl(pnl: Double): String =
    if (pnl >= 0) "+$" + String.format("%.0f", pnl) else "-$" + String.format("%.0f", -pnl)
