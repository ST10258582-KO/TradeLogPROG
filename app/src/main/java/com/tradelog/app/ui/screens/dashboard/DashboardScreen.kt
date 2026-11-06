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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradelog.app.R
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.model.TradeDirection
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
    val isLoading by tradeViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d(TAG, "DashboardScreen launched")
        tradeViewModel.loadTrades()
    }

    // Compute stats live from real trades
    val closed = trades.filter { it.pnl != null }
    val wins = closed.count { (it.pnl ?: 0.0) > 0 }
    val winRate = if (closed.isEmpty()) null else wins.toDouble() / closed.size
    val netPnl = if (closed.isEmpty()) null else closed.sumOf { it.pnl ?: 0.0 }
    val avgR = closed.mapNotNull { it.rMultiple }.let { rs -> if (rs.isEmpty()) null else rs.average() }

    // Build equity curve: cumulative P&L sorted by entry time
    val equityCurve = run {
        var cumulative = 0.0
        closed.sortedBy { it.entryTime }.map { t ->
            cumulative += t.pnl ?: 0.0
            EquityCurvePoint(t.entryTime.take(10), cumulative)
        }
    }

    // Offline mode banner — shown when device has no network connectivity
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    @Suppress("DEPRECATION")
    val isOnline = connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        if (!isOnline) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE65100),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.offline_banner),
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        item { SummaryRow(winRate = winRate, netPnl = netPnl, avgR = avgR) }
        item { EquityCurveCard(points = equityCurve) }
        item {
            Text(
                stringResource(R.string.recent_trades),
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
private fun SummaryRow(winRate: Double?, netPnl: Double?, avgR: Double?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(stringResource(R.string.win_rate).uppercase(), winRate?.let { "${(it * 100).toInt()}%" } ?: "--", modifier = Modifier.weight(1f))
        StatCard(
            stringResource(R.string.net_pnl).uppercase(),
            netPnl?.let { formatPnl(it) } ?: "--",
            valueColor = when {
                netPnl == null -> Color.White
                netPnl >= 0 -> NeonGreen
                else -> LossRed
            },
            modifier = Modifier.weight(1f)
        )
        StatCard(stringResource(R.string.avg_r).uppercase(), avgR?.let { String.format("%.2fR", it) } ?: "--", modifier = Modifier.weight(1f))
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
            Text(stringResource(R.string.equity_curve), style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(12.dp))
            if (points.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.loading), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
