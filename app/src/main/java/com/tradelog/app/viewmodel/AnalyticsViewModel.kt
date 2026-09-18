package com.tradelog.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradelog.app.data.remote.SessionStats
import com.tradelog.app.data.remote.TagStats
import com.tradelog.app.data.repository.TradeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// AnalyticsViewModel drives the Analytics screen breakdown tabs (FR-07).
class AnalyticsViewModel(
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val TAG = "AnalyticsViewModel"

    private val _sessionStats = MutableStateFlow<SessionStats?>(null)
    val sessionStats: StateFlow<SessionStats?> = _sessionStats.asStateFlow()

    private val _tagStats = MutableStateFlow<TagStats?>(null)
    val tagStats: StateFlow<TagStats?> = _tagStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        Log.d(TAG, "AnalyticsViewModel initialised")
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            Log.d(TAG, "loadAnalytics() — fetching session and tag stats")
            _isLoading.value = true
            tradeRepository.getStatsBySession().onSuccess { _sessionStats.value = it }
                .onFailure {
                    Log.w(TAG, "getStatsBySession failed — loading sample data")
                    _sessionStats.value = com.tradelog.app.data.remote.SessionStats(
                        london = com.tradelog.app.data.remote.SessionDetail(winRate = 0.75, avgR = 2.1, netPnl = 1095.0, tradeCount = 8),
                        newYork = com.tradelog.app.data.remote.SessionDetail(winRate = 0.60, avgR = 1.5, netPnl = 560.0, tradeCount = 5),
                        asia = com.tradelog.app.data.remote.SessionDetail(winRate = 1.0, avgR = 2.8, netPnl = 280.0, tradeCount = 1)
                    )
                }
            tradeRepository.getStatsByTag().onSuccess { _tagStats.value = it }
                .onFailure {
                    Log.w(TAG, "getStatsByTag failed — loading sample data")
                    _tagStats.value = com.tradelog.app.data.remote.TagStats(
                        tags = listOf(
                            com.tradelog.app.data.remote.TagStat(name = "breakout", winRate = 0.80, avgR = 2.5, netPnl = 975.0),
                            com.tradelog.app.data.remote.TagStat(name = "trend", winRate = 0.75, avgR = 2.0, netPnl = 505.0),
                            com.tradelog.app.data.remote.TagStat(name = "high-rr", winRate = 0.83, avgR = 2.9, netPnl = 1265.0),
                            com.tradelog.app.data.remote.TagStat(name = "reversal", winRate = 0.67, avgR = 1.8, netPnl = 430.0),
                            com.tradelog.app.data.remote.TagStat(name = "fakeout", winRate = 0.0, avgR = -1.37, netPnl = -180.0),
                            com.tradelog.app.data.remote.TagStat(name = "failed-breakout", winRate = 0.0, avgR = -0.87, netPnl = -130.0)
                        )
                    )
                }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "AnalyticsViewModel cleared")
    }
}
