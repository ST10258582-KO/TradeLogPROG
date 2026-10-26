package com.tradelog.app.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tradelog.app.data.local.AppDatabase
import com.tradelog.app.data.repository.FirestoreRepository
import kotlinx.coroutines.tasks.await
import java.time.Instant

// SyncWorker runs in the background and uploads locally-modified trades to Firestore
// once a network connection is available. This provides the offline-sync architecture
// required by the POE "Offline Mode with Synchronisation" feature.
// Reference: Google LLC, "WorkManager" Android Developers (2024)
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val TAG = "SyncWorker"

    /** Local Room database — the offline source of truth. */
    private val db = AppDatabase.getInstance(context)

    /** Firestore repository used to push pending changes to the cloud. */
    private val firestoreRepo = FirestoreRepository()

    /**
     * Entry point called by WorkManager on a background thread.
     * Returns [Result.success] when all pending records are synced,
     * [Result.retry] on any exception so WorkManager applies exponential back-off.
     */
    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "doWork() — no authenticated user, skipping sync")
            return Result.success()
        }

        Log.d(TAG, "doWork() — syncing pending trades for user $userId")

        return try {
            syncPendingTrades(userId)
            syncPendingJournalEntries(userId)
            Log.i(TAG, "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            Result.retry()   // WorkManager will retry with exponential back-off
        }
    }

    /**
     * Push all locally-modified trades to Firestore, then mark them as synced in Room.
     * Individual trade failures are logged but do not abort the remaining batch.
     */
    private suspend fun syncPendingTrades(userId: String) {
        val pending = db.tradeDao().getPendingSyncTrades(userId)
        Log.d(TAG, "syncPendingTrades() — ${pending.size} pending trade(s)")
        pending.forEach { entity ->
            val trade = entity.toDomain()
            val syncResult = firestoreRepo.saveTrade(userId, trade)
            if (syncResult.isSuccess) {
                val now = Instant.now().toString()
                db.tradeDao().markSynced(entity.id, now)
                Log.i(TAG, "Trade ${entity.id} synced to Firestore")
            } else {
                Log.w(TAG, "Failed to sync trade ${entity.id}: ${syncResult.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Push all locally-modified journal entries to Firestore under users/{uid}/journal/{entryId}.
     * Individual entry failures are logged and silently skipped to avoid blocking other syncs.
     */
    private suspend fun syncPendingJournalEntries(userId: String) {
        val pending = db.journalDao().getPendingSyncEntries(userId)
        Log.d(TAG, "syncPendingJournalEntries() — ${pending.size} pending entry/entries")
        pending.forEach { entry ->
            try {
                // Write journal entry to Firestore (users/{uid}/journal/{entryId})
                Firebase.firestore
                    .collection("users")
                    .document(userId)
                    .collection("journal")
                    .document(entry.id)
                    .set(
                        mapOf(
                            "userId" to entry.userId,
                            "date" to entry.date,
                            "premarketNotes" to entry.premarketNotes,
                            "postmarketNotes" to entry.postmarketNotes,
                            "mood" to entry.mood,
                            "rulesFollowed" to entry.rulesFollowed,
                            "createdAt" to entry.createdAt,
                            "updatedAt" to entry.updatedAt
                        )
                    )
                    .await()
                db.journalDao().markSynced(entry.id, Instant.now().toString())
                Log.i(TAG, "Journal entry ${entry.id} synced")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync journal entry ${entry.id}: ${e.message}")
            }
        }
    }
}
