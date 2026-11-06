package com.tradelog.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.model.TradeDirection
import com.tradelog.app.data.model.TradingSession
import com.tradelog.app.data.model.TradeStatus
import kotlinx.coroutines.tasks.await

// FirestoreRepository handles all Firestore NoSQL read and write operations.
// Data is organised under: users/{uid}/trades/{tradeId}
class FirestoreRepository {

    private val TAG = "FirestoreRepository"

    /** Firestore instance used for all database operations. */
    private val db: FirebaseFirestore = Firebase.firestore

    /**
     * Write a trade document to Firestore under the user's collection.
     * @param userId The authenticated user's UID.
     * @param trade  The domain Trade to persist.
     * @return [Result.success] on write success, [Result.failure] with the exception otherwise.
     */
    suspend fun saveTrade(userId: String, trade: Trade): Result<Unit> {
        return try {
            Log.d(TAG, "saveTrade() — userId=$userId tradeId=${trade.id}")
            val doc = tradeToMap(trade)
            db.collection("users")
                .document(userId)
                .collection("trades")
                .document(trade.id)
                .set(doc)
                .await()
            Log.i(TAG, "Trade ${trade.id} written to Firestore successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "saveTrade failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Read all trade documents from Firestore for a given user.
     * @param userId The authenticated user's UID.
     * @return [Result.success] with a list of trades, or [Result.failure] on error.
     */
    suspend fun getTrades(userId: String): Result<List<Trade>> {
        return try {
            Log.d(TAG, "getTrades() — userId=$userId")
            val snapshot = db.collection("users")
                .document(userId)
                .collection("trades")
                .get()
                .await()
            val trades = snapshot.documents.mapNotNull { doc ->
                try {
                    mapToTrade(doc.id, doc.data ?: return@mapNotNull null)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse trade ${doc.id}: ${e.message}")
                    null
                }
            }
            Log.i(TAG, "Fetched ${trades.size} trades from Firestore")
            Result.success(trades)
        } catch (e: Exception) {
            Log.e(TAG, "getTrades failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a trade document from Firestore.
     * @param userId  The authenticated user's UID.
     * @param tradeId The unique trade ID to remove.
     * @return [Result.success] on deletion, [Result.failure] with the exception otherwise.
     */
    suspend fun deleteTrade(userId: String, tradeId: String): Result<Unit> {
        return try {
            Log.d(TAG, "deleteTrade() — tradeId=$tradeId")
            db.collection("users").document(userId).collection("trades").document(tradeId).delete().await()
            Log.i(TAG, "Trade $tradeId deleted from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteTrade failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Serialise a [Trade] domain model to a Firestore-compatible [Map].
     * Nullable fields are included so Firestore can store null and later overwrite them.
     */
    private fun tradeToMap(trade: Trade): Map<String, Any?> = mapOf(
        "userId" to trade.userId,
        "instrument" to trade.instrument,
        "direction" to trade.direction.name,
        "entryPrice" to trade.entryPrice,
        "stopLoss" to trade.stopLoss,
        "takeProfit" to trade.takeProfit,
        "exitPrice" to trade.exitPrice,
        "quantity" to trade.quantity,
        "rMultiple" to trade.rMultiple,
        "pnl" to trade.pnl,
        "session" to trade.session?.name,
        "status" to trade.status.name,
        "notes" to trade.notes,
        "entryTime" to trade.entryTime,
        "exitTime" to trade.exitTime,
        "tags" to trade.tags,
        "createdAt" to trade.createdAt,
        "syncedAt" to trade.syncedAt
    )

    /**
     * Deserialise a Firestore document [Map] back to a [Trade] domain model.
     * Missing or null fields fall back to safe defaults to avoid crashes on schema changes.
     */
    private fun mapToTrade(id: String, data: Map<String, Any?>): Trade = Trade(
        id = id,
        userId = data["userId"] as? String ?: "",
        instrument = data["instrument"] as? String ?: "",
        direction = TradeDirection.valueOf(data["direction"] as? String ?: "BUY"),
        entryPrice = (data["entryPrice"] as? Number)?.toDouble() ?: 0.0,
        stopLoss = (data["stopLoss"] as? Number)?.toDouble() ?: 0.0,
        takeProfit = (data["takeProfit"] as? Number)?.toDouble() ?: 0.0,
        exitPrice = (data["exitPrice"] as? Number)?.toDouble(),
        quantity = (data["quantity"] as? Number)?.toDouble() ?: 1.0,
        rMultiple = (data["rMultiple"] as? Number)?.toDouble(),
        pnl = (data["pnl"] as? Number)?.toDouble(),
        session = (data["session"] as? String)?.let { TradingSession.valueOf(it) },
        status = TradeStatus.valueOf(data["status"] as? String ?: "OPEN"),
        notes = data["notes"] as? String,
        entryTime = data["entryTime"] as? String ?: "",
        exitTime = data["exitTime"] as? String,
        tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        createdAt = data["createdAt"] as? String ?: "",
        syncedAt = data["syncedAt"] as? String
    )
}
