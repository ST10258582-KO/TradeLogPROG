package com.tradelog.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.tradelog.app.data.local.dao.TradeDao
import com.tradelog.app.data.local.entities.TradeEntity
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.model.TradeDirection
import com.tradelog.app.data.model.TradingSession
import com.tradelog.app.data.model.TradeStatus
import com.tradelog.app.data.remote.AnalyticsSummary
import com.tradelog.app.data.remote.EquityCurveResponse
import com.tradelog.app.data.remote.RetrofitClient
import com.tradelog.app.data.remote.SessionStats
import com.tradelog.app.data.remote.TagStats
import com.tradelog.app.data.remote.TradeRequest
import com.tradelog.app.data.remote.TradeResponse

// TradeRepository handles all trade data operations via the REST API (FR-04 to FR-07).
// Falls back to the local Room cache when the network is unavailable (offline mode).
// All API calls are made as suspend functions — called from ViewModel coroutine scopes.
class TradeRepository(private val tradeDao: TradeDao? = null) {

    private val TAG = "TradeRepository"
    private val api get() = RetrofitClient.apiService

    // Fetch paginated list of trades with optional filters (FR-05).
    // On success: results are cached to Room for offline access.
    // On failure: serves the last-known trades from the Room cache.
    suspend fun getTrades(
        page: Int = 1,
        limit: Int = 20,
        tag: String? = null,
        session: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<List<Trade>> {
        return try {
            Log.d(TAG, "Fetching trades — page=$page tag=$tag session=$session")
            val response = api.getTrades(page, limit, tag, session, dateFrom, dateTo)
            if (response.isSuccessful && response.body() != null) {
                val trades = response.body()!!.trades.map { it.toDomain() }
                Log.d(TAG, "Fetched ${trades.size} trades — caching to Room")
                tradeDao?.insertOrReplaceAll(trades.map { TradeEntity.fromDomain(it) })
                Result.success(trades)
            } else {
                Log.w(TAG, "getTrades HTTP ${response.code()} — serving cache")
                serveCachedTrades()
            }
        } catch (e: Exception) {
            Log.w(TAG, "getTrades offline (${e.message}) — serving cache")
            serveCachedTrades()
        }
    }

    private suspend fun serveCachedTrades(): Result<List<Trade>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (tradeDao != null && uid != null) {
            val cached = tradeDao.getTradesByUser(uid).map { it.toDomain() }
            Log.d(TAG, "Serving ${cached.size} cached trade(s) from Room")
            return Result.success(cached)
        }
        return Result.failure(Exception("Offline and no local cache available"))
    }

    // Create a new trade entry (FR-04). On success, also caches to Room.
    suspend fun createTrade(request: TradeRequest): Result<Trade> {
        return try {
            Log.d(TAG, "Creating trade: ${request.instrument} ${request.direction}")
            val response = api.createTrade(request)
            if (response.isSuccessful && response.body() != null) {
                val trade = response.body()!!.toDomain()
                Log.i(TAG, "Trade created: ${trade.id}")
                tradeDao?.insertOrReplace(TradeEntity.fromDomain(trade))
                Result.success(trade)
            } else {
                Log.e(TAG, "createTrade failed: HTTP ${response.code()}")
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "createTrade error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fetch a single trade by ID (FR-06)
    suspend fun getTrade(id: String): Result<Trade> {
        return try {
            Log.d(TAG, "Fetching trade: $id")
            val response = api.getTrade(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTrade error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Update an existing trade (FR-06). On success, also updates Room cache.
    suspend fun updateTrade(id: String, request: TradeRequest): Result<Trade> {
        return try {
            Log.d(TAG, "Updating trade: $id")
            val response = api.updateTrade(id, request)
            if (response.isSuccessful && response.body() != null) {
                val trade = response.body()!!.toDomain()
                Log.i(TAG, "Trade updated: $id")
                tradeDao?.insertOrReplace(TradeEntity.fromDomain(trade))
                Result.success(trade)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateTrade error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete a trade permanently (FR-06). On success, also removes from Room cache.
    suspend fun deleteTrade(id: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting trade: $id")
            val response = api.deleteTrade(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Trade deleted: $id")
                tradeDao?.deleteById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteTrade error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Analytics endpoints (FR-07)
    suspend fun getAnalyticsSummary(dateFrom: String? = null, dateTo: String? = null): Result<AnalyticsSummary> {
        return try {
            val response = api.getAnalyticsSummary(dateFrom, dateTo)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("HTTP ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getEquityCurve(dateFrom: String? = null, dateTo: String? = null): Result<EquityCurveResponse> {
        return try {
            val response = api.getEquityCurve(dateFrom, dateTo)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("HTTP ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getStatsBySession(): Result<SessionStats> {
        return try {
            val response = api.getStatsBySession()
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("HTTP ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getStatsByTag(): Result<TagStats> {
        return try {
            val response = api.getStatsByTag()
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("HTTP ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Maps API response DTO → domain model
    private fun TradeResponse.toDomain() = Trade(
        id = id, userId = userId, instrument = instrument,
        direction = if (direction == "BUY") TradeDirection.BUY else TradeDirection.SELL,
        entryPrice = entryPrice, stopLoss = stopLoss, takeProfit = takeProfit,
        exitPrice = exitPrice, quantity = quantity, rMultiple = rMultiple, pnl = pnl,
        session = session?.let {
            when (it) {
                "LONDON" -> TradingSession.LONDON
                "NEW_YORK" -> TradingSession.NEW_YORK
                "ASIA" -> TradingSession.ASIA
                else -> TradingSession.OTHER
            }
        },
        status = if (status == "CLOSED") TradeStatus.CLOSED else TradeStatus.OPEN,
        notes = notes, entryTime = entryTime, exitTime = exitTime,
        tags = tags, createdAt = createdAt, syncedAt = syncedAt
    )
}
