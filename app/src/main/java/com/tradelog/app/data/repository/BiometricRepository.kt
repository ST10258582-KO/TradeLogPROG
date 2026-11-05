package com.tradelog.app.data.repository

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// BiometricRepository handles fingerprint and face authentication prompts.
// Uses the AndroidX Biometric library for consistent cross-API behaviour (API 23+).
// This satisfies the POE requirement: "Biometric Authentication."
// Reference: Google LLC, "Biometric Authentication" Android Developers (2024)
class BiometricRepository {

    private val TAG = "BiometricRepository"

    /**
     * Sealed class representing every possible outcome of a biometric authentication attempt.
     * Callers should exhaustively handle all variants when collecting the result.
     */
    sealed class BiometricResult {
        /** Authentication succeeded — the user's identity is confirmed. */
        object Success : BiometricResult()

        /** The device has biometric hardware but no credentials are enrolled. */
        object NotEnrolled : BiometricResult()

        /** The device does not support biometric authentication. */
        object NotAvailable : BiometricResult()

        /**
         * The authentication attempt produced an unrecoverable error.
         * @param message Human-readable error string from the BiometricPrompt API.
         */
        data class Error(val message: String) : BiometricResult()
    }

    /**
     * Check whether the device supports and has enrolled biometrics.
     * @param context Any valid [Context] — does not need to be an Activity.
     * @return true if [BiometricManager.BIOMETRIC_SUCCESS] is returned, false otherwise.
     */
    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val result = manager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        Log.d(TAG, "canAuthenticate() = $result")
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show the biometric prompt and suspend until the user authenticates or cancels.
     * Must be called from a [FragmentActivity] (e.g. AppCompatActivity) on the main thread.
     *
     * @param activity The host activity used to attach the BiometricPrompt fragment.
     * @return A [BiometricResult] describing the outcome of the authentication attempt.
     */
    suspend fun authenticate(activity: FragmentActivity): BiometricResult {
        Log.d(TAG, "authenticate() — showing biometric prompt")

        if (!canAuthenticate(activity)) {
            Log.w(TAG, "Biometrics not available or not enrolled")
            return BiometricResult.NotAvailable
        }

        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                /** Called when the user is successfully verified — resume coroutine with Success. */
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.i(TAG, "Biometric authentication succeeded")
                    if (continuation.isActive) continuation.resume(BiometricResult.Success)
                }

                /** Called on a non-recoverable error such as too many failed attempts or user cancellation. */
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.w(TAG, "Biometric error $errorCode: $errString")
                    if (continuation.isActive) continuation.resume(
                        BiometricResult.Error(errString.toString())
                    )
                }

                /**
                 * Called for each individual failed attempt (wrong finger, etc.).
                 * The prompt handles retries automatically — do NOT resume the coroutine here.
                 */
                override fun onAuthenticationFailed() {
                    Log.d(TAG, "Biometric attempt failed — waiting for retry")
                }
            }

            val prompt = BiometricPrompt(activity, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify your identity")
                .setSubtitle("Use your fingerprint or face to access TradeLog")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                .build()

            // Cancel the prompt if the coroutine is cancelled externally (e.g. screen rotation)
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            prompt.authenticate(promptInfo)
        }
    }
}
