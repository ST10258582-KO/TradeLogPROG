package com.tradelog.app.ui.screens.riskcalc

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tradelog.app.R
import com.tradelog.app.ui.theme.Purple
import kotlin.math.abs

private const val TAG = "RiskCalculatorScreen"

// Risk Calculator Screen.
// Allows traders to calculate exact position size, dollar risk, and R:R ratio
// given their account balance, risk percentage, and stop loss distance.
// Operates fully offline — no network required.
@Composable
fun RiskCalculatorScreen(onBack: () -> Unit) {

    // ── Input state ────────────────────────────────────────────────────────────
    var accountBalance by remember { mutableStateOf("10000") }
    var riskPercent by remember { mutableStateOf("1.0") }
    var stopPips by remember { mutableStateOf("20") }
    var pipValue by remember { mutableStateOf("10") }
    var targetPips by remember { mutableStateOf("40") }

    // ── Computed results ───────────────────────────────────────────────────────
    data class CalcResult(
        val dollarRisk: Double,
        val positionSize: Double,
        val rewardDollars: Double,
        val rrRatio: Double
    )

    var result by remember { mutableStateOf<CalcResult?>(null) }

    fun calculate() {
        val balance = accountBalance.toDoubleOrNull() ?: return
        val riskPct = riskPercent.toDoubleOrNull() ?: return
        val stop = stopPips.toDoubleOrNull() ?: return
        val pip = pipValue.toDoubleOrNull() ?: return
        val target = targetPips.toDoubleOrNull() ?: return

        if (stop <= 0 || pip <= 0 || balance <= 0 || riskPct <= 0) return

        // Dollar risk = account balance × risk%
        val dollarRisk = balance * (riskPct / 100.0)
        // Position size (lots) = dollar risk ÷ (stop pips × pip value per lot)
        val positionSize = dollarRisk / (stop * pip)
        // Reward in dollars based on target pips
        val rewardDollars = positionSize * target * pip
        // R:R ratio
        val rrRatio = if (dollarRisk > 0) rewardDollars / dollarRisk else 0.0

        result = CalcResult(dollarRisk, positionSize, rewardDollars, rrRatio)
        Log.d(TAG, "calculate() — dollarRisk=$${"%.2f".format(dollarRisk)} " +
                "posSize=${"%.2f".format(positionSize)} lots RR=${"%.2f".format(rrRatio)}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
            Text(
                stringResource(R.string.risk_calc_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Input card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Position Inputs",
                    style = MaterialTheme.typography.titleMedium,
                    color = Purple,
                    fontWeight = FontWeight.SemiBold
                )
                CalcInputField(stringResource(R.string.account_balance), accountBalance) { accountBalance = it }
                CalcInputField(stringResource(R.string.risk_percent), riskPercent) { riskPercent = it }
                CalcInputField(stringResource(R.string.stop_pips), stopPips) { stopPips = it }
                CalcInputField(stringResource(R.string.pip_value), pipValue) { pipValue = it }
                CalcInputField("Take Profit (pips/points)", targetPips) { targetPips = it }

                Button(
                    onClick = { calculate() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.calculate), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Results card — shown after calculation
        result?.let { r ->
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Calculation Results",
                        style = MaterialTheme.typography.titleMedium,
                        color = Purple,
                        fontWeight = FontWeight.SemiBold
                    )
                    ResultRow(stringResource(R.string.dollar_risk), "$${"%.2f".format(r.dollarRisk)}")
                    ResultRow(stringResource(R.string.position_size), "${"%.2f".format(r.positionSize)} lots")
                    ResultRow("Reward", "$${"%.2f".format(r.rewardDollars)}")
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.risk_to_reward), color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            "1 : ${"%.1f".format(r.rrRatio)}",
                            color = if (r.rrRatio >= 2.0) Color(0xFF4CAF50) else if (r.rrRatio >= 1.0) Color(0xFFFF9800) else Color(0xFFF44336),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CalcInputField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple,
            focusedLabelColor = Purple,
            cursorColor = Purple,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
