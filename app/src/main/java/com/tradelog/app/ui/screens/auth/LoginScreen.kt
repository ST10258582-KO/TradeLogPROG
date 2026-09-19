package com.tradelog.app.ui.screens.auth

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradelog.app.R
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.AuthViewModel

private const val TAG = "LoginScreen"

// Login screen — entry point for returning users (FR-02).
// Uses Android Credential Manager to launch Google Sign-In and authenticate via Firebase.
// Reference: Google LLC, "Sign in with Google using Credential Manager" (2024)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val activity = LocalActivity.current

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            Log.d(TAG, "Auth success — navigating to Dashboard")
            onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.drawable.ic_tradelog),
                contentDescription = "TradeLog logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Sign in to access your trading journal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            // Google Sign-In button (FR-02)
            Button(
                onClick = {
                    Log.d(TAG, "Google Sign-In button tapped")
                    activity?.let { authViewModel.signIn(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = authState !is AuthViewModel.AuthState.Loading
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Sign In with Google",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (authState is AuthViewModel.AuthState.Error) {
                Text(
                    (authState as AuthViewModel.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            }

            TextButton(onClick = {
                Log.d(TAG, "Navigating to Register")
                onNavigateToRegister()
            }) {
                Text("Don't have an account? Register", color = Purple)
            }
        }
    }
}
