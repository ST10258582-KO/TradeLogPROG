package com.tradelog.app.ui.screens.trades

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.tradelog.app.data.remote.TradeRequest
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.SettingsViewModel
import com.tradelog.app.viewmodel.TradeViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "TradeEntryScreen"

// Trade Entry form — full-screen form for logging a new trade (FR-04).
// Captures instrument, direction, entry/stop/TP, position size, strategy tags, and notes.
// R-multiple and session classification are computed server-side.
@Composable
fun TradeEntryScreen(
    tradeViewModel: TradeViewModel,
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    editTradeId: String? = null
) {
    val existingTrade by tradeViewModel.selectedTrade.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val isEditing = editTradeId != null && editTradeId != "new"

    var instrument by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("BUY") }
    var entryPrice by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var takeProfit by remember { mutableStateOf("") }
    var exitPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pnlInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedStrategies by remember { mutableStateOf(setOf<String>()) }
    var selectedSession by remember { mutableStateOf("") }
    var instrumentExpanded by remember { mutableStateOf(false) }
    var sessionExpanded by remember { mutableStateOf(false) }

    // Pre-populate fields when editing
    LaunchedEffect(editTradeId) {
        if (isEditing) tradeViewModel.loadTrade(editTradeId!!)
    }
    LaunchedEffect(existingTrade) {
        val t = existingTrade
        if (isEditing && t != null && t.id == editTradeId) {
            instrument = t.instrument
            direction = t.direction.name
            entryPrice = t.entryPrice.toString()
            stopLoss = t.stopLoss.toString()
            takeProfit = t.takeProfit.toString()
            exitPrice = t.exitPrice?.toString() ?: ""
            quantity = t.quantity.toString()
            pnlInput = t.pnl?.toString() ?: ""
            notes = t.notes ?: ""
            selectedStrategies = t.tags.toSet()
            selectedSession = t.session?.name ?: ""
        }
    }
    // Validation errors
    var instrumentError by remember { mutableStateOf<String?>(null) }
    var entryError by remember { mutableStateOf<String?>(null) }
    var slError by remember { mutableStateOf<String?>(null) }
    var tpError by remember { mutableStateOf<String?>(null) }
    var qtyError by remember { mutableStateOf<String?>(null) }

    val isLoading by tradeViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (isEditing) "Edit Trade" else "Log Trade", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Instrument dropdown
            ExposedDropdownMenuBox(expanded = instrumentExpanded, onExpandedChange = { instrumentExpanded = it }) {
                OutlinedTextField(
                    value = instrument,
                    onValueChange = { instrument = it.uppercase(); instrumentError = null },
                    label = { Text("Instrument") },
                    isError = instrumentError != null,
                    supportingText = { instrumentError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instrumentExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = outlinedColors()
                )
                ExposedDropdownMenu(expanded = instrumentExpanded, onDismissRequest = { instrumentExpanded = false }) {
                    settings.instruments.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { instrument = option; instrumentError = null; instrumentExpanded = false }
                        )
                    }
                }
            }

            // Direction toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BUY", "SELL").forEach { dir ->
                    Button(
                        onClick = { direction = dir },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (direction == dir) Purple else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(dir, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Price fields
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriceField("Entry Price", entryPrice, entryError, Modifier.weight(1f)) { entryPrice = it; entryError = null }
                PriceField("Stop Loss", stopLoss, slError, Modifier.weight(1f)) { stopLoss = it; slError = null }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriceField("Take Profit", takeProfit, tpError, Modifier.weight(1f)) { takeProfit = it; tpError = null }
                PriceField("Exit Price (opt.)", exitPrice, null, Modifier.weight(1f)) { exitPrice = it }
            }

            PriceField("Position Size / Qty", quantity, qtyError, Modifier.fillMaxWidth()) { quantity = it; qtyError = null }

            PriceField("P&L ($) (opt.)", pnlInput, null, Modifier.fillMaxWidth()) { pnlInput = it }

            // Strategy multi-select chips
            Text("Strategy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                settings.strategies.forEach { strat ->
                    val selected = selectedStrategies.contains(strat)
                    FilterChip(
                        selected = selected,
                        onClick = { selectedStrategies = if (selected) selectedStrategies - strat else selectedStrategies + strat },
                        label = { Text(strat, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Purple, selectedLabelColor = Color.White)
                    )
                }
            }

            // Session dropdown
            ExposedDropdownMenuBox(expanded = sessionExpanded, onExpandedChange = { sessionExpanded = it }) {
                OutlinedTextField(
                    value = selectedSession,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Session (opt.)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = outlinedColors()
                )
                ExposedDropdownMenu(expanded = sessionExpanded, onDismissRequest = { sessionExpanded = false }) {
                    DropdownMenuItem(text = { Text("Auto-detect") }, onClick = { selectedSession = ""; sessionExpanded = false })
                    settings.sessions.forEach { session ->
                        DropdownMenuItem(text = { Text(session) }, onClick = { selectedSession = session; sessionExpanded = false })
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4,
                colors = outlinedColors()
            )

            Spacer(Modifier.height(8.dp))

            // Submit
            Button(
                onClick = {
                    // Input validation (NFR-05)
                    var valid = true
                    if (instrument.isBlank()) { instrumentError = "Required"; valid = false }
                    val entry = entryPrice.toDoubleOrNull(); if (entry == null) { entryError = "Invalid"; valid = false }
                    val sl = stopLoss.toDoubleOrNull(); if (sl == null) { slError = "Invalid"; valid = false }
                    val tp = takeProfit.toDoubleOrNull(); if (tp == null) { tpError = "Invalid"; valid = false }
                    val qty = quantity.toDoubleOrNull(); if (qty == null || qty <= 0) { qtyError = "Invalid"; valid = false }
                    if (!valid) return@Button

                    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val tags = selectedStrategies.toList()

                    val request = TradeRequest(
                        instrument = instrument,
                        direction = direction,
                        entryPrice = entry!!,
                        stopLoss = sl!!,
                        takeProfit = tp!!,
                        exitPrice = exitPrice.toDoubleOrNull(),
                        quantity = qty!!,
                        pnl = pnlInput.toDoubleOrNull(),
                        notes = notes.ifBlank { null },
                        tags = tags,
                        entryTime = now,
                        exitTime = null
                    )
                    if (isEditing) {
                        Log.d(TAG, "Updating trade: $editTradeId")
                        tradeViewModel.updateTrade(editTradeId!!, request, onSuccess = { onDismiss() })
                    } else {
                        Log.d(TAG, "Submitting trade: $instrument $direction entry=$entry")
                        tradeViewModel.createTrade(request, onSuccess = { onDismiss() })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (isEditing) "SAVE CHANGES" else "LOG TRADE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun PriceField(label: String, value: String, error: String?, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        colors = outlinedColors()
    )
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Purple,
    focusedLabelColor = Purple,
    cursorColor = Purple
)
