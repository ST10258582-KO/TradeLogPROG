package com.tradelog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradelog.app.data.local.entities.JournalEntity
import kotlinx.coroutines.flow.Flow

// Data Access Object for the journal_entries table.
@Dao
interface JournalDao {

    // Observe all journal entries for a user as a reactive Flow
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun observeEntriesByUser(userId: String): Flow<List<JournalEntity>>

    // Get entry for a specific date — used to load today's journal
    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getEntryForDate(userId: String, date: String): JournalEntity?

    // Get entries awaiting Firestore sync
    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND isPendingSync = 1")
    suspend fun getPendingSyncEntries(userId: String): List<JournalEntity>

    // Insert or replace a single journal entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entry: JournalEntity)

    // Batch insert or replace — used for initial sync download from Firestore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(entries: List<JournalEntity>)

    // Clear all journal entries for a user — used on sign-out to protect data privacy
    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun clearForUser(userId: String)

    // Mark a journal entry as synced — called after SyncWorker successfully uploads to Firestore
    @Query("UPDATE journal_entries SET isPendingSync = 0, updatedAt = :updatedAt WHERE id = :entryId")
    suspend fun markSynced(entryId: String, updatedAt: String)
}
