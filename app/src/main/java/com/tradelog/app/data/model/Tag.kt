package com.tradelog.app.data.model

// Domain model for strategy tags (e.g. ORB, Breakout, Mean Reversion).
// Maps to the Tag table in Part 1 Planning Document Section 7.3.
data class Tag(
    val id: String,
    val userId: String,
    val name: String
)
