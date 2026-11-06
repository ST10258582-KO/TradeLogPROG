package com.tradelog.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tradelog.app.data.local.AppDatabase
import com.tradelog.app.data.model.Trade
import com.tradelog.app.data.remote.AnalyticsSummary
import com.tradelog.app.data.remote.EquityCurvePoint
import com.tradelog.app.data.remote.TradeRequest
import com.tradelog.app.data.repository.TradeRepository
import com.tradelog.app.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TradeViewModel manages trade list, analytics summary, and equity curve data.
// Drives the Dashboard, Trades List, Trade Entry, and Trade Detail screens (FR-04 to FR-07).
// Uses AndroidViewModel to access Room database for offline caching.
class TradeViewModel(application: Application) : AndroidViewModel(application) {

    private val tradeRepository = TradeRepository(AppDatabase.getInstance(application).tradeDao())

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

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        Log.d(TAG, "TradeViewModel initialised — loading trades")
        loadTrades()
    }

    // Load all trades with optional filters (FR-05)
    fun loadTrades(tag: String? = null, session: String? = null) {
        viewModelScope.launch {
            Log.d(TAG, "loadTrades() — tag=$tag session=$session")
            _isLoading.value = true
            tradeRepository.getTrades(tag = tag, session = session).fold(
                onSuccess = { result ->
                    _trades.value = when {
                        session != null -> result.filter { it.session?.name == session }
                        tag != null -> result.filter { it.tags.contains(tag) }
                        else -> result
                    }
                },
                onFailure = {
                    Log.w(TAG, "loadTrades failed: ${it.message}")
                    _trades.value = emptyList()
                }
            )
            _isLoading.value = false
        }
    }

    // No-op kept so call sites in DashboardScreen compile — stats computed in UI from trades
    fun loadDashboard() {
        loadTrades()
    }

    // Load a single trade for the detail screen (FR-06)
    fun loadTrade(id: String) {
        viewModelScope.launch {
            Log.d(TAG, "loadTrade($id)")
            _selectedTrade.value = null
            _isDetailLoading.value = true
            tradeRepository.getTrade(id).fold(
                onSuccess = { _selectedTrade.value = it },
                onFailure = {
                    Log.w(TAG, "loadTrade failed: ${it.message}")
                    _errorMessage.value = it.message
                }
            )
            _isDetailLoading.value = false
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
                    NotificationHelper.showTradeLoggedNotification(getApplication(), it.instrument, it.pnl)
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
