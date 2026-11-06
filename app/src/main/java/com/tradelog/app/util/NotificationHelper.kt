package com.tradelog.app.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.tradelog.app.R
import com.tradelog.app.TradeLogApplication

// Utility for posting local in-app notifications (POE: Real-time Notifications).
// Fires when a trade is successfully logged so the user gets immediate feedback.
object NotificationHelper {

    fun showTradeLoggedNotification(context: Context, instrument: String, pnl: Double?) {
        val pnlText = when {
            pnl == null -> ""
            pnl >= 0 -> " · +\$${String.format("%.0f", pnl)}"
            else -> " · -\$${String.format("%.0f", -pnl)}"
        }
        val notification = NotificationCompat.Builder(context, TradeLogApplication.CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Trade Logged")
            .setContentText("$instrument trade saved successfully$pnlText")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
