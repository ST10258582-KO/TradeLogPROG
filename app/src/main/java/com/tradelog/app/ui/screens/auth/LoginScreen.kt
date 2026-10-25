package com.tradelog.app.ui.screens.auth

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.tradelog.app.R
import com.tradelog.app.data.repository.BiometricRepository
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

// Login screen — entry point for returning users (FR-02).
// Supports Google Sign-In via Credential Manager and optional Biometric quick-unlock.
// Reference: Google LLC, "Sign in with Google using Credential Manager" (2024)
// Reference: Google LLC, "Biometric Authentication" Android Developers (2024)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Check if device supports biometrics — only show button if available
    val biometricAvailable = remember {
        BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    var biometricError by remember { mutableStateOf<String?>(null) }

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
                stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.sign_in_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            // Google Sign-In button (FR-02)
            Button(
                onClick = {
                    Log.d(TAG, "Google Sign-In button tapped")
                    biometricError = null
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
                        stringResource(R.string.sign_in_google),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            // Biometric quick-unlock button — only shown when device supports biometrics
            // This satisfies the POE Biometric Authentication requirement
            if (biometricAvailable) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        Log.d(TAG, "Biometric sign-in tapped")
                        biometricError = null
                        scope.launch {
                            val fragActivity = activity as? FragmentActivity
                            if (fragActivity == null) {
                                biometricError = "Biometric not available"
                                return@launch
                            }
                            val repo = BiometricRepository()
                            val result = repo.authenticate(fragActivity)
                            when (result) {
                                is BiometricRepository.BiometricResult.Success -> {
                                    Log.i(TAG, "Biometric success — navigating to Dashboard")
                                    onNavigateToDashboard()
                                }
                                is BiometricRepository.BiometricResult.Error -> {
                                    Log.w(TAG, "Biometric error: ${result.message}")
                                    biometricError = result.message
                                }
                                else -> {
                                    Log.d(TAG, "Biometric not available/enrolled")
                                    biometricError = "Biometric authentication not available"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Purple)
                    )
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.biometric_sign_in),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Show auth or biometric errors
            val errorMsg = (authState as? AuthViewModel.AuthState.Error)?.message ?: biometricError
            if (errorMsg != null) {
                Text(
                    errorMsg,
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
                Text(stringResource(R.string.no_account), color = Purple)
            }
        }
    }
}
