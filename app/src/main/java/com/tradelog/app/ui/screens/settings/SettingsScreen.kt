package com.tradelog.app.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tradelog.app.data.repository.UserSettings
import com.tradelog.app.ui.theme.LossRed
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.AuthViewModel
import com.tradelog.app.viewmodel.SettingsViewModel

private const val TAG = "SettingsScreen"

// Settings screen — allows users to modify their profile and preferences (FR-03).
// All settings persist locally via DataStore and sync to the REST API.
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onSignOut: () -> Unit
) {
    val settings by settingsViewModel.settings.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Local mutable copies for the form
    var displayName by remember(settings.displayName) { mutableStateOf(settings.displayName) }
    var timezone by remember(settings.timezone) { mutableStateOf(settings.timezone) }
    var defaultRisk by remember(settings.defaultRisk) { mutableStateOf(settings.defaultRisk.toString()) }
    var currency by remember(settings.currency) { mutableStateOf(settings.currency) }
    var language by remember(settings.language) { mutableStateOf(settings.language) }
    var notifications by remember(settings.notifications) { mutableStateOf(settings.notifications) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d(TAG, "SettingsScreen launched — user: ${currentUser?.email}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        // User profile card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Purple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (currentUser?.displayName?.firstOrNull() ?: 'T').toString().uppercase(),
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(currentUser?.displayName ?: "Trader", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(currentUser?.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Settings form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsTextField("Display Name", displayName) { displayName = it; saved = false }
                SettingsTextField("Timezone", timezone) { timezone = it; saved = false }
                SettingsTextField("Default Risk %", defaultRisk) { defaultRisk = it; saved = false }
                SettingsTextField("Currency", currency) { currency = it; saved = false }

                // Language selector (FR-03 — English, Afrikaans, Zulu)
                Text("Language", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en" to "English", "af" to "Afrikaans", "zu" to "Zulu").forEach { (code, label) ->
                        FilterChip(
                            selected = language == code,
                            onClick = { language = code; saved = false },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Notifications toggle
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Notifications", style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = notifications,
                        onCheckedChange = { notifications = it; saved = false },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple)
                    )
                }
            }
        }

        // Save button
        Button(
            onClick = {
                val risk = defaultRisk.toFloatOrNull() ?: settings.defaultRisk
                Log.d(TAG, "Saving settings — displayName=$displayName language=$language notifications=$notifications")
                settingsViewModel.saveSettings(UserSettings(displayName, timezone, risk, currency, language, notifications))
                saved = true
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (saved) MaterialTheme.colorScheme.surfaceVariant else Purple)
        ) {
            Text(if (saved) "Saved" else "Save Changes", fontWeight = FontWeight.SemiBold)
        }

        // Sign out
        OutlinedButton(
            onClick = {
                Log.i(TAG, "Sign-out tapped")
                authViewModel.signOut()
                onSignOut()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(LossRed))
        ) {
            Text("Sign Out", color = LossRed, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple,
            focusedLabelColor = Purple,
            cursorColor = Purple
        )
    )
}
