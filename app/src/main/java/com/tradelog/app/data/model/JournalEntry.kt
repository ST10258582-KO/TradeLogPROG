package com.tradelog.app.data.model

data class JournalEntry(
    val id: String,
    val date: String,           // "yyyy-MM-dd"
    val checklistCompleted: Int,
    val checklistTotal: Int,
    val notes: String
)
