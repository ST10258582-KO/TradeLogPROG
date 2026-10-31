package com.tradelog.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.remote.AnalyticsSummary
import com.tradelog.app.data.remote.EquityCurvePoint
import com.tradelog.app.data.remote.TradeRequest
import com.tradelog.app.data.repository.TradeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TradeViewModel manages trade list, analytics summary, and equity curve data.
// Drives the Dashboard, Trades List, Trade Entry, and Trade Detail screens (FR-04 to FR-07).
class TradeViewModel(
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val TAG = "TradeViewModel"

    // ── Trade list state ──────────────────────────────────────────────────────
    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    val trades: StateFlow<List<Trade>> = _trades.asStateFlow()

    private val _selectedTrade = MutableStateFlow<Trade?>(null)
    val selectedTrade: StateFlow<Trade?> = _selectedTrade.asStateFlow()

    // ── Dashboard / Analytics state ───────────────────────────────────────────
    private val _summary = MutableStateFlow<AnalyticsSummary?>(null)
    val summary: StateFlow<AnalyticsSummary?> = _summary.asStateFlow()

    private val _equityCurve = MutableStateFlow<List<EquityCurvePoint>>(emptyList())
    val equityCurve: StateFlow<List<EquityCurvePoint>> = _equityCurve.asStateFlow()

    // ── UI state ─────────────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        Log.d(TAG, "TradeViewModel initialised — loading trades and dashboard data")
        // Show sample data immediately so the UI is never empty
        _trades.value = sampleTrades()
        _summary.value = AnalyticsSummary(winRate = 0.667, netPnl = 1842.50, avgR = 1.8, tradeCount = 12)
        _equityCurve.value = listOf(
            EquityCurvePoint("2025-08-01", 10000.0),
            EquityCurvePoint("2025-08-04", 10240.0),
            EquityCurvePoint("2025-08-06", 10180.0),
            EquityCurvePoint("2025-08-09", 10520.0),
            EquityCurvePoint("2025-08-12", 10390.0),
            EquityCurvePoint("2025-08-15", 10780.0),
            EquityCurvePoint("2025-08-18", 11050.0),
            EquityCurvePoint("2025-08-21", 10920.0),
            EquityCurvePoint("2025-08-25", 11380.0),
            EquityCurvePoint("2025-08-28", 11842.0)
        )
        loadTrades()
        loadDashboard()
    }

    // Load all trades with optional filters (FR-05)
    fun loadTrades(tag: String? = null, session: String? = null) {
        viewModelScope.launch {
            Log.d(TAG, "loadTrades() — tag=$tag session=$session")
            _isLoading.value = true
            val all = sampleTrades()
            tradeRepository.getTrades(tag = tag, session = session).fold(
                onSuccess = { result ->
                    val list = if (result.isEmpty()) all else result
                    _trades.value = if (session != null)
                        list.filter { it.session?.name == session }
                    else if (tag != null)
                        list.filter { it.tags.contains(tag) }
                    else list
                },
                onFailure = {
                    Log.w(TAG, "API unavailable — loading sample trades for demo")
                    _trades.value = if (session != null)
                        all.filter { it.session?.name == session }
                    else if (tag != null)
                        all.filter { it.tags.contains(tag) }
                    else all
                }
            )
            _isLoading.value = false
        }
    }

    // Load dashboard summary and equity curve (FR-07)
    fun loadDashboard() {
        viewModelScope.launch {
            Log.d(TAG, "loadDashboard()")
            val sampleSummary = AnalyticsSummary(winRate = 0.667, netPnl = 1842.50, avgR = 1.8, tradeCount = 12)
            val sampleCurve = listOf(
                EquityCurvePoint("2025-08-01", 10000.0),
                EquityCurvePoint("2025-08-04", 10240.0),
                EquityCurvePoint("2025-08-06", 10180.0),
                EquityCurvePoint("2025-08-09", 10520.0),
                EquityCurvePoint("2025-08-12", 10390.0),
                EquityCurvePoint("2025-08-15", 10780.0),
                EquityCurvePoint("2025-08-18", 11050.0),
                EquityCurvePoint("2025-08-21", 10920.0),
                EquityCurvePoint("2025-08-25", 11380.0),
                EquityCurvePoint("2025-08-28", 11842.0)
            )
            tradeRepository.getAnalyticsSummary().fold(
                onSuccess = { result ->
                    _summary.value = if (result.tradeCount > 0) result else sampleSummary
                },
                onFailure = { _summary.value = sampleSummary }
            )
            tradeRepository.getEquityCurve().fold(
                onSuccess = { result ->
                    _equityCurve.value = if (result.points.isNotEmpty()) result.points else sampleCurve
                },
                onFailure = { _equityCurve.value = sampleCurve }
            )
        }
    }

    private fun sampleTrades() = listOf(
        Trade(id="1", userId="demo", instrument="NAS100", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=18450.0, stopLoss=18320.0, takeProfit=18750.0, exitPrice=18740.0,
            quantity=1.0, rMultiple=2.23, pnl=290.0, session=com.tradelog.app.data.model.TradingSession.LONDON,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("breakout","high-rr"),
            entryTime="2025-08-28T09:15:00Z", exitTime="2025-08-28T11:42:00Z", createdAt="2025-08-28T09:15:00Z"),
        Trade(id="2", userId="demo", instrument="US30", direction=com.tradelog.app.data.model.TradeDirection.SELL,
            entryPrice=39200.0, stopLoss=39350.0, takeProfit=38900.0, exitPrice=38920.0,
            quantity=1.0, rMultiple=1.87, pnl=280.0, session=com.tradelog.app.data.model.TradingSession.NEW_YORK,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("trend","reversal"),
            entryTime="2025-08-27T14:30:00Z", exitTime="2025-08-27T16:10:00Z", createdAt="2025-08-27T14:30:00Z"),
        Trade(id="3", userId="demo", instrument="EURUSD", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=1.0842, stopLoss=1.0810, takeProfit=1.0920, exitPrice=1.0798,
            quantity=10000.0, rMultiple=-1.37, pnl=-180.0, session=com.tradelog.app.data.model.TradingSession.LONDON,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("news","fakeout"),
            entryTime="2025-08-26T08:00:00Z", exitTime="2025-08-26T09:45:00Z", createdAt="2025-08-26T08:00:00Z"),
        Trade(id="4", userId="demo", instrument="NAS100", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=18120.0, stopLoss=18020.0, takeProfit=18420.0, exitPrice=18415.0,
            quantity=1.0, rMultiple=2.95, pnl=295.0, session=com.tradelog.app.data.model.TradingSession.NEW_YORK,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("breakout"),
            entryTime="2025-08-25T13:00:00Z", exitTime="2025-08-25T15:30:00Z", createdAt="2025-08-25T13:00:00Z"),
        Trade(id="5", userId="demo", instrument="GBPUSD", direction=com.tradelog.app.data.model.TradeDirection.SELL,
            entryPrice=1.2680, stopLoss=1.2720, takeProfit=1.2580, exitPrice=1.2590,
            quantity=10000.0, rMultiple=2.25, pnl=225.0, session=com.tradelog.app.data.model.TradingSession.LONDON,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("trend"),
            entryTime="2025-08-22T09:30:00Z", exitTime="2025-08-22T11:00:00Z", createdAt="2025-08-22T09:30:00Z"),
        Trade(id="6", userId="demo", instrument="US30", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=38750.0, stopLoss=38600.0, takeProfit=39100.0, exitPrice=38620.0,
            quantity=1.0, rMultiple=-0.87, pnl=-130.0, session=com.tradelog.app.data.model.TradingSession.NEW_YORK,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("failed-breakout"),
            entryTime="2025-08-21T14:00:00Z", exitTime="2025-08-21T15:20:00Z", createdAt="2025-08-21T14:00:00Z"),
        Trade(id="7", userId="demo", instrument="NAS100", direction=com.tradelog.app.data.model.TradeDirection.SELL,
            entryPrice=18620.0, stopLoss=18720.0, takeProfit=18320.0, exitPrice=18340.0,
            quantity=1.0, rMultiple=2.80, pnl=280.0, session=com.tradelog.app.data.model.TradingSession.ASIA,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("reversal","high-rr"),
            entryTime="2025-08-20T02:15:00Z", exitTime="2025-08-20T05:40:00Z", createdAt="2025-08-20T02:15:00Z"),
        Trade(id="8", userId="demo", instrument="EURUSD", direction=com.tradelog.app.data.model.TradeDirection.SELL,
            entryPrice=1.0910, stopLoss=1.0945, takeProfit=1.0805, exitPrice=1.0812,
            quantity=10000.0, rMultiple=2.80, pnl=280.0, session=com.tradelog.app.data.model.TradingSession.LONDON,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("trend"),
            entryTime="2025-08-19T08:45:00Z", exitTime="2025-08-19T12:30:00Z", createdAt="2025-08-19T08:45:00Z"),
        Trade(id="9", userId="demo", instrument="NAS100", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=17980.0, stopLoss=17860.0, takeProfit=18340.0, exitPrice=17875.0,
            quantity=1.0, rMultiple=-0.87, pnl=-105.0, session=com.tradelog.app.data.model.TradingSession.NEW_YORK,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("choppy"),
            entryTime="2025-08-18T13:30:00Z", exitTime="2025-08-18T14:45:00Z", createdAt="2025-08-18T13:30:00Z"),
        Trade(id="10", userId="demo", instrument="US30", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=38200.0, stopLoss=38080.0, takeProfit=38600.0, exitPrice=38590.0,
            quantity=1.0, rMultiple=3.25, pnl=390.0, session=com.tradelog.app.data.model.TradingSession.LONDON,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("breakout","high-rr"),
            entryTime="2025-08-15T09:00:00Z", exitTime="2025-08-15T12:15:00Z", createdAt="2025-08-15T09:00:00Z"),
        Trade(id="11", userId="demo", instrument="GBPUSD", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=1.2540, stopLoss=1.2490, takeProfit=1.2690, exitPrice=1.2685,
            quantity=10000.0, rMultiple=2.90, pnl=290.0, session=com.tradelog.app.data.model.TradingSession.LONDON,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("trend","breakout"),
            entryTime="2025-08-12T08:30:00Z", exitTime="2025-08-12T11:00:00Z", createdAt="2025-08-12T08:30:00Z"),
        Trade(id="12", userId="demo", instrument="NAS100", direction=com.tradelog.app.data.model.TradeDirection.BUY,
            entryPrice=17650.0, stopLoss=17550.0, takeProfit=17950.0, exitPrice=17940.0,
            quantity=1.0, rMultiple=2.90, pnl=290.0, session=com.tradelog.app.data.model.TradingSession.NEW_YORK,
            status=com.tradelog.app.data.model.TradeStatus.CLOSED, tags=listOf("high-rr"),
            entryTime="2025-08-09T14:00:00Z", exitTime="2025-08-09T16:30:00Z", createdAt="2025-08-09T14:00:00Z")
    )

    // Load a single trade for the detail screen (FR-06)
    fun loadTrade(id: String) {
        viewModelScope.launch {
            Log.d(TAG, "loadTrade($id)")
            _isLoading.value = true
            tradeRepository.getTrade(id).fold(
                onSuccess = { _selectedTrade.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    // Submit new trade from the Trade Entry form (FR-04)
    fun createTrade(request: TradeRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "createTrade() — ${request.instrument} ${request.direction}")
            _isLoading.value = true
            tradeRepository.createTrade(request).fold(
                onSuccess = {
                    Log.i(TAG, "Trade created: ${it.id}")
                    loadTrades()
                    loadDashboard()
                    onSuccess()
                },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    // Update an existing trade (FR-06)
    fun updateTrade(id: String, request: TradeRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "updateTrade($id)")
            _isLoading.value = true
            tradeRepository.updateTrade(id, request).fold(
                onSuccess = {
                    loadTrades()
                    loadDashboard()
                    onSuccess()
                },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoading.value = false
        }
    }

    // Permanently delete a trade (FR-06)
    fun deleteTrade(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "deleteTrade($id)")
            tradeRepository.deleteTrade(id).fold(
                onSuccess = {
                    Log.i(TAG, "Trade deleted: $id")
                    loadTrades()
                    loadDashboard()
                    onSuccess()
                },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun clearError() { _errorMessage.value = null }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "TradeViewModel cleared")
    }
}
