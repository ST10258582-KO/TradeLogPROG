package com.tradelog.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.tradelog.app.data.model.User
import com.tradelog.app.data.remote.RegisterRequest
import com.tradelog.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

// AuthRepository manages all Firebase authentication state and operations.
// Uses Firebase Authentication with Google Sign-In (FR-01, FR-02).
// Reference: Firebase, "Firebase Authentication" (Google LLC, 2024)
class AuthRepository {

    private val TAG = "AuthRepository"
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isLoggedIn: Boolean get() = _currentUser.value != null

    init {
        // Restore session if user is already signed in to Firebase (FR-02)
        firebaseAuth.currentUser?.let { firebaseUser ->
            Log.d(TAG, "Restoring existing Firebase session: ${firebaseUser.email}")
            _currentUser.value = firebaseUser.toDomain()
        }
    }

    // Signs in with a Google ID token obtained from the Credential Manager flow.
    // Called from AuthViewModel after the Google Sign-In launcher returns a result.
    // Reference: Google LLC, "Sign in with Google" developer docs (2024)
    suspend fun signInWithGoogleIdToken(idToken: String): Result<User> {
        return try {
            Log.d(TAG, "Signing in with Google ID token")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Firebase sign-in returned no user"))

            // Retrieve Firebase ID token to authenticate with our REST API
            val apiToken = firebaseUser.getIdToken(false).await().token
            RetrofitClient.authToken = apiToken
            Log.d(TAG, "Firebase ID token obtained and set on RetrofitClient")

            val user = firebaseUser.toDomain()
            _currentUser.value = user
            Log.i(TAG, "Sign-in successful: ${user.email} (isNewUser=${result.additionalUserInfo?.isNewUser})")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogleIdToken failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Registers a new user profile via the REST API after first sign-in (FR-01).
    // Only called for new users (isNewUser == true from Firebase result).
    suspend fun registerUser(
        uid: String,
        displayName: String,
        email: String,
        timezone: String,
        defaultRisk: Float
    ): Result<User> {
        return try {
            Log.d(TAG, "Registering new user: $email")
            val response = RetrofitClient.apiService.register(
                RegisterRequest(uid, displayName, email, timezone, defaultRisk)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = User(
                    id = body.id, email = body.email,
                    displayName = body.displayName, timezone = body.timezone,
                    defaultRisk = body.defaultRisk, currency = body.currency,
                    language = body.language
                )
                _currentUser.value = user
                Log.i(TAG, "Registration successful: ${user.email}")
                Result.success(user)
            } else {
                val msg = "Registration failed: HTTP ${response.code()}"
                Log.e(TAG, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        Log.i(TAG, "Signing out user: ${_currentUser.value?.email}")
        firebaseAuth.signOut()
        _currentUser.value = null
        RetrofitClient.authToken = null
    }

    // Maps a FirebaseUser to the app's domain User model
    private fun FirebaseUser.toDomain() = User(
        id = uid,
        email = email ?: "",
        displayName = displayName ?: "Trader",
        timezone = "Africa/Johannesburg",
        defaultRisk = 1.0f,
        currency = "USD",
        language = "en"
    )
}
