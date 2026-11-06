package com.tradelog.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.tradelog.app.ui.navigation.AppNavigation
import com.tradelog.app.ui.theme.TradeLogTheme

// Single Activity — hosts the entire Jetpack Compose UI.
// All navigation is handled by the Jetpack Navigation Compose NavHost inside AppNavigation.
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    // POST_NOTIFICATIONS runtime permission launcher — required on Android 13+ (API 33+).
    // Without this grant the NotificationManager silently drops all notifications.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.i(TAG, "POST_NOTIFICATIONS permission result: granted=$granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate — TradeLog app starting")

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Allow content to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            TradeLogTheme {
                AppNavigation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart — activity visible")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume — activity in foreground")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause — activity losing focus")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop — activity no longer visible")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy — activity destroyed")
    }
}
