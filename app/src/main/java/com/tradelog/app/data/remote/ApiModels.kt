package com.tradelog.app.data.remote

// ─────────────────────────────────────────────────────────────────────────────
// API Data Transfer Objects (DTOs) for TradeLog REST API
// These map 1-to-1 with the endpoint specification in Part 1 Section 5.2.
// Serialisation handled by Gson (Google, 2024).
// ─────────────────────────────────────────────────────────────────────────────

// Auth
data class RegisterRequest(
    val uid: String,
    val displayName: String,
    val email: String,
    val timezone: String,
    val defaultRisk: Float
)

data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val timezone: String,
    val defaultRisk: Float,
    val currency: String,
    val language: String,
    val createdAt: String
)

// Trades
data class TradeRequest(
    val instrument: String,
    val direction: String,       // "BUY" | "SELL"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double?,
    val quantity: Double,
    val notes: String?,
    val tags: List<String>,
    val entryTime: String,       // ISO-8601
    val exitTime: String?
)

data class TradeResponse(
    val id: String,
    val userId: String,
    val instrument: String,
    val direction: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double?,
    val quantity: Double,
    val rMultiple: Double?,
    val pnl: Double?,
    val session: String?,        // "LONDON" | "NEW_YORK" | "ASIA" | "OTHER"
    val status: String,          // "OPEN" | "CLOSED"
    val notes: String?,
    val entryTime: String,
    val exitTime: String?,
    val tags: List<String>,
    val createdAt: String,
    val syncedAt: String?
)

data class TradesListResponse(
    val trades: List<TradeResponse>,
    val total: Int
)

// Analytics
data class AnalyticsSummary(
    val winRate: Double,
    val netPnl: Double,
    val avgR: Double,
    val tradeCount: Int
)

data class EquityCurvePoint(
    val date: String,
    val cumulativePnl: Double
)

data class EquityCurveResponse(
    val points: List<EquityCurvePoint>
)

data class SessionDetail(
    val winRate: Double,
    val avgR: Double,
    val netPnl: Double,
    val tradeCount: Int
)

data class SessionStats(
    val london: SessionDetail,
    val newYork: SessionDetail,
    val asia: SessionDetail
)

data class TagStat(
    val name: String,
    val winRate: Double,
    val avgR: Double,
    val netPnl: Double
)

data class TagStats(
    val tags: List<TagStat>
)

// Tags
data class TagRequest(val name: String)

data class TagResponse(
    val id: String,
    val userId: String,
    val name: String
)

data class TagsListResponse(val tags: List<TagResponse>)

// Settings
data class SettingsResponse(
    val displayName: String,
    val timezone: String,
    val defaultRisk: Float,
    val currency: String,
    val language: String,
    val notifications: Boolean
)

data class DeleteResponse(val success: Boolean)
