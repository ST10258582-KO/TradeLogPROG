package com.tradelog.app.data.model

// Domain model representing a TradeLog user profile.
// Maps to the User table defined in Part 1 Planning Document Section 7.1.
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val timezone: String = "Africa/Johannesburg",
    val defaultRisk: Float = 1.0f,
    val currency: String = "USD",
    val language: String = "en"
)
