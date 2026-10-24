package com.tradelog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradelog.app.data.local.entities.TradeEntity
import kotlinx.coroutines.flow.Flow

// Data Access Object for the trades table.
// All queries are suspend functions or Flows for safe coroutine-based access.
// Reference: Google LLC, "Room DAO" Android Developers (2024)
@Dao
interface TradeDao {

    // Observe all trades for a specific user as a reactive Flow — UI auto-updates on changes
    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeTradesByUser(userId: String): Flow<List<TradeEntity>>

    // Get all trades once (non-reactive) — used by SyncWorker for batch operations
    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getTradesByUser(userId: String): List<TradeEntity>

    // Get trades that have been modified locally but not yet synced to Firestore
    @Query("SELECT * FROM trades WHERE userId = :userId AND isPendingSync = 1")
    suspend fun getPendingSyncTrades(userId: String): List<TradeEntity>

    // Get a single trade by its unique identifier
    @Query("SELECT * FROM trades WHERE id = :tradeId LIMIT 1")
    suspend fun getTradeById(tradeId: String): TradeEntity?

    // Insert or replace — used when syncing down from Firestore or saving new local trade
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(trade: TradeEntity)

    // Batch insert or replace — used for initial sync download from Firestore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(trades: List<TradeEntity>)

    // Delete a trade by ID — called after successful deletion on Firestore
    @Query("DELETE FROM trades WHERE id = :tradeId")
    suspend fun deleteById(tradeId: String)

    // Clear all trades for a user — used on sign-out to protect data privacy
    @Query("DELETE FROM trades WHERE userId = :userId")
    suspend fun clearForUser(userId: String)

    // Mark a trade as synced — called after SyncWorker successfully uploads to Firestore
    @Query("UPDATE trades SET isPendingSync = 0, syncedAt = :syncedAt WHERE id = :tradeId")
    suspend fun markSynced(tradeId: String, syncedAt: String)
}
