package com.tradelog.app.data.model

// Domain model representing a single trade journal entry.
// Maps to the Trade table defined in Part 1 Planning Document Section 7.2.
data class Trade(
    val id: String,
    val userId: String,
    val instrument: String,
    val direction: TradeDirection,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double? = null,
    val quantity: Double,
    /** R-multiple: computed server-side from (exitPrice - entryPrice) / (entryPrice - stopLoss) */
    val rMultiple: Double? = null,
    /** Realised P&L in the user's preferred currency, computed server-side */
    val pnl: Double? = null,
    /** Auto-classified by entry timestamp on the server (FR-04) */
    val session: TradingSession? = null,
    val status: TradeStatus = TradeStatus.OPEN,
    val notes: String? = null,
    val entryTime: String,
    val exitTime: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val syncedAt: String? = null
)

enum class TradeDirection { BUY, SELL }

enum class TradingSession { LONDON, NEW_YORK, ASIA, OTHER }

enum class TradeStatus { OPEN, CLOSED }
