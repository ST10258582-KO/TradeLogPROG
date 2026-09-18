package com.tradelog.app.viewmodel

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tradelog.app.data.model.User
import com.tradelog.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// AuthViewModel manages authentication state and drives the Login / Register screens.
// Uses Android Credential Manager for Google Sign-In (FR-01, FR-02).
// Reference: Google LLC, "Sign in with Google using Credential Manager" (2024)
// Reference: Google LLC, "Guide to App Architecture" Android Developers (2024)
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "AuthViewModel"

    // Web client ID from Firebase console — required for Google Sign-In token request
    // Found in google-services.json under oauth_client where client_type == 3
    companion object {
        // TODO: Replace with your actual Web Client ID from Firebase Console >
        // Authentication > Sign-in method > Google > Web SDK configuration
        const val WEB_CLIENT_ID = "908728194206-h9l7jvso3qs5sgvklgoidj5us4amqat4.apps.googleusercontent.com"
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    // Launches Google Sign-In via Credential Manager and authenticates with Firebase (FR-02)
    fun signIn(activity: Activity) {
        viewModelScope.launch {
            Log.d(TAG, "signIn() called — initiating Google Sign-In via Credential Manager")
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(activity)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(result.credential.data)
                    .idToken

                Log.d(TAG, "Google ID token received — signing in with Firebase")
                val authResult = authRepository.signInWithGoogleIdToken(googleIdToken)
                _authState.value = if (authResult.isSuccess) {
                    Log.i(TAG, "Firebase authentication successful")
                    AuthState.Success(authResult.getOrThrow())
                } else {
                    val msg = authResult.exceptionOrNull()?.message ?: "Sign-in failed"
                    Log.e(TAG, "Firebase authentication failed: $msg")
                    AuthState.Error(msg)
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager error: ${e.message}", e)
                _authState.value = AuthState.Error("Google Sign-In was cancelled or failed")
            } catch (e: Exception) {
                Log.e(TAG, "signIn error: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Called on first-time registration to complete the user profile (FR-01)
    fun register(displayName: String, timezone: String, defaultRisk: Float) {
        viewModelScope.launch {
            Log.d(TAG, "register() — displayName=$displayName")
            _authState.value = AuthState.Loading
            val user = currentUser.value ?: run {
                _authState.value = AuthState.Error("No authenticated user found")
                return@launch
            }
            val result = authRepository.registerUser(
                uid = user.id,
                displayName = displayName,
                email = user.email,
                timezone = timezone,
                defaultRisk = defaultRisk
            )
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrThrow())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun signOut() {
        Log.i(TAG, "signOut() called")
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "AuthViewModel cleared")
    }
}
