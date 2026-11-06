package com.tradelog.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.model.TradeDirection
import com.tradelog.app.data.model.TradingSession
import com.tradelog.app.data.model.TradeStatus

// Room entity that persists Trade data locally for offline-first architecture.
// Mirrors the domain Trade model but uses flat primitive fields for Room compatibility.
@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val instrument: String,
    val direction: String,         // Stored as string enum name
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double?,
    val quantity: Double,
    val rMultiple: Double?,
    val pnl: Double?,
    val session: String?,          // Stored as string enum name
    val status: String,            // Stored as string enum name
    val notes: String?,
    val entryTime: String,
    val exitTime: String?,
    val tags: String,              // JSON-serialised list of tag strings
    val createdAt: String,
    val syncedAt: String?,
    val screenshotUrl: String?,    // Firebase Storage URL for attached chart screenshot
    val isPendingSync: Boolean = false   // True when local changes not yet synced to Firestore
) {
    // Convert Room entity back to domain model for use in ViewModels and UI
    fun toDomain(): Trade = Trade(
        id = id, userId = userId, instrument = instrument,
        direction = TradeDirection.valueOf(direction),
        entryPrice = entryPrice, stopLoss = stopLoss, takeProfit = takeProfit,
        exitPrice = exitPrice, quantity = quantity, rMultiple = rMultiple, pnl = pnl,
        session = session?.let { TradingSession.valueOf(it) },
        status = TradeStatus.valueOf(status),
        notes = notes, entryTime = entryTime, exitTime = exitTime,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        createdAt = createdAt, syncedAt = syncedAt
    )

    companion object {
        // Convert domain Trade to Room entity for local persistence
        fun fromDomain(trade: Trade, screenshotUrl: String? = null, isPendingSync: Boolean = false) = TradeEntity(
            id = trade.id, userId = trade.userId, instrument = trade.instrument,
            direction = trade.direction.name, entryPrice = trade.entryPrice,
            stopLoss = trade.stopLoss, takeProfit = trade.takeProfit,
            exitPrice = trade.exitPrice, quantity = trade.quantity,
            rMultiple = trade.rMultiple, pnl = trade.pnl,
            session = trade.session?.name, status = trade.status.name,
            notes = trade.notes, entryTime = trade.entryTime, exitTime = trade.exitTime,
            tags = trade.tags.joinToString(","), createdAt = trade.createdAt,
            syncedAt = trade.syncedAt, screenshotUrl = screenshotUrl,
            isPendingSync = isPendingSync
        )
    }
}
