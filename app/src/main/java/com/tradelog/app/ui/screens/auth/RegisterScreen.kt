package com.tradelog.app.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.AuthViewModel

private const val TAG = "RegisterScreen"

// Register screen — collects profile details from new users on first login (FR-01).
// Only asks for what's needed upfront; risk settings live in Settings.
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashboard: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("Africa/Johannesburg") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            Log.d(TAG, "Registration success — navigating to Dashboard")
            onNavigateToDashboard()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Set up your trading profile",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it; nameError = null },
            label = { Text("Display Name") },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                focusedLabelColor = Purple
            )
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = timezone,
            onValueChange = { timezone = it },
            label = { Text("Timezone") },
            placeholder = { Text("e.g. Africa/Johannesburg") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                focusedLabelColor = Purple
            )
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (displayName.isBlank()) {
                    nameError = "Display name is required"
                    return@Button
                }
                Log.d(TAG, "Register tapped — displayName=$displayName timezone=$timezone")
                // Default risk starts at 1% — user can change it in Settings
                authViewModel.register(displayName, timezone, defaultRisk = 1.0f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            enabled = authState !is AuthViewModel.AuthState.Loading
        ) {
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                (authState as AuthViewModel.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
