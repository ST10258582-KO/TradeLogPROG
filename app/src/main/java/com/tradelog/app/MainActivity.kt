package com.tradelog.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.tradelog.app.ui.navigation.AppNavigation
import com.tradelog.app.ui.theme.TradeLogTheme

// Single Activity — hosts the entire Jetpack Compose UI.
// All navigation is handled by the Jetpack Navigation Compose NavHost inside AppNavigation.
// Reference: Google LLC, "Guide to App Architecture" Android Developers (2024)
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate — TradeLog app starting")

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
