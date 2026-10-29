package com.tradelog.app.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tradelog.app.MainActivity
import com.tradelog.app.R
import com.tradelog.app.TradeLogApplication

// Firebase Cloud Messaging service — handles incoming push notifications from FCM.
// This satisfies the POE requirement: "Real-time Notifications via Firebase Cloud Messaging."
// Reference: Firebase, "Firebase Cloud Messaging for Android" (Google LLC, 2024)
class TradeLogMessagingService : FirebaseMessagingService() {

    private val TAG = "TradeLogMsgService"

    // Called when a new FCM registration token is generated.
    // The token should be sent to your server to target this device with push messages.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token generated — length=${token.length}")
        // In production: send this token to your REST API to store against the user account
        // so the server can send targeted push notifications to this device
    }

    // Called when a push message is received while the app is in the foreground.
    // For background messages, the system shows a default notification automatically.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived() — from=${remoteMessage.from}")

        // Log all received data for diagnostics
        remoteMessage.data.entries.forEach { (key, value) ->
            Log.d(TAG, "  data: $key = $value")
        }

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "TradeLog"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "You have a new notification"
        val type = remoteMessage.data["type"] ?: "general"

        Log.i(TAG, "Showing notification — type=$type title='$title'")
        showNotification(title, body, type)
    }

    // Build and display a local notification using the data from the FCM message
    private fun showNotification(title: String, body: String, type: String) {
        // Determine the correct notification channel based on message type
        val channelId = when (type) {
            "trade_alert" -> TradeLogApplication.CHANNEL_ID_ALERTS
            "journal_reminder" -> TradeLogApplication.CHANNEL_ID_REMINDERS
            else -> TradeLogApplication.CHANNEL_ID_ALERTS
        }

        // Tap-to-open intent — launches the app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (type == "trade_alert") NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        val notificationId = System.currentTimeMillis().toInt()
        manager.notify(notificationId, notification)
        Log.d(TAG, "Notification posted — id=$notificationId channel=$channelId")
    }
}
