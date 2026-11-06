package com.tradelog.app.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

// NotificationRepository handles FCM token retrieval and topic subscriptions.
// Topic subscriptions allow targeted push notifications to groups of users.
class NotificationRepository {

    private val TAG = "NotificationRepository"

    // Retrieve the current FCM registration token for this device.
    // This token uniquely identifies the device and must be sent to your server
    // to enable targeting this specific device with push notifications.
    suspend fun getDeviceToken(): Result<String> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.i(TAG, "FCM device token retrieved (length=${token.length})")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Subscribe this device to the "trade_alerts" topic.
    // Any FCM message sent to topic "trade_alerts" will be delivered to all subscribers.
    suspend fun subscribeToAlerts(): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic("trade_alerts").await()
            Log.i(TAG, "Subscribed to FCM topic: trade_alerts")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "subscribeToAlerts failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Subscribe to daily journal reminder notifications
    suspend fun subscribeToReminders(): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic("journal_reminders").await()
            Log.i(TAG, "Subscribed to FCM topic: journal_reminders")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "subscribeToReminders failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Unsubscribe from all notification topics — called when user disables notifications
    suspend fun unsubscribeAll(): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("trade_alerts").await()
            FirebaseMessaging.getInstance().unsubscribeFromTopic("journal_reminders").await()
            Log.i(TAG, "Unsubscribed from all FCM topics")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "unsubscribeAll failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
