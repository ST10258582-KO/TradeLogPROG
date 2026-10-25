package com.tradelog.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tradelog.app.workers.SyncWorker
import java.util.concurrent.TimeUnit

// Application class — initialises app-wide singletons and schedules background work.
// Registered in AndroidManifest.xml via android:name=".TradeLogApplication".
// Reference: Google LLC, "Application class" Android Developers (2024)
class TradeLogApplication : Application() {

    private val TAG = "TradeLogApplication"

    /**
     * Called when the application process is created.
     * Initialises notification channels (required on API 26+) and queues the periodic sync worker.
     */
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "TradeLogApplication onCreate — initialising app components")
        createNotificationChannels()
        scheduleSyncWorker()
    }

    /**
     * Create notification channels required for Android 8.0+ (API 26+).
     * Channels created here are persistent and only need to be registered once per install.
     * Reference: Google LLC, "Create and Manage Notification Channels" (2024)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Trade Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time trade notifications and market alerts"
            }
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to log your trades and journal entries"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(alertsChannel)
            manager.createNotificationChannel(reminderChannel)
            Log.d(TAG, "Notification channels created")
        }
    }

    /**
     * Schedule the periodic background sync worker.
     * Runs every 15 minutes when any network connection is available.
     * [ExistingPeriodicWorkPolicy.KEEP] prevents duplicate workers if the app is relaunched.
     * WorkManager handles rescheduling automatically after device reboots.
     */
    private fun scheduleSyncWorker() {
        Log.d(TAG, "Scheduling periodic SyncWorker")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        Log.i(TAG, "SyncWorker scheduled — will run every 15 min when connected")
    }

    companion object {
        /** Notification channel ID for real-time trade alerts. */
        const val CHANNEL_ID_ALERTS = "tradelog_alerts"

        /** Notification channel ID for daily journal reminder notifications. */
        const val CHANNEL_ID_REMINDERS = "tradelog_reminders"

        /** Unique WorkManager work name — prevents duplicate SyncWorker instances. */
        private const val SYNC_WORK_NAME = "tradelog_periodic_sync"
    }
}
