package com.tradelog.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity that persists daily journal entries locally for offline access.
// Users can write pre/post-market notes even when there is no network connection.
@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: String,              // ISO date string "YYYY-MM-DD"
    val premarketNotes: String,
    val postmarketNotes: String,
    val mood: Int,                 // 1-5 scale: trader's confidence/emotional state
    val rulesFollowed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val isPendingSync: Boolean = false   // True when local changes not yet uploaded to Firestore
)
