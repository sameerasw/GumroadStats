package com.sameerasw.gumroadstats.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sameerasw.gumroadstats.data.local.PayoutsCache
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.data.preferences.PreferencesManager
import com.sameerasw.gumroadstats.data.preferences.UpdateInterval
import com.sameerasw.gumroadstats.data.repository.GumroadRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class PayoutsUiState {
    object Initial : PayoutsUiState()
    object Loading : PayoutsUiState()
    data class Success(val payouts: List<Payout>, val isOfflineData: Boolean = false) : PayoutsUiState()
    data class Error(val message: String) : PayoutsUiState()
}

sealed class PayoutDetailsState {
    object Idle : PayoutDetailsState()
    object Loading : PayoutDetailsState()
    data class Success(val payout: Payout) : PayoutDetailsState()
    data class Error(val message: String) : PayoutDetailsState()
}

class PayoutsViewModel(context: Context) : ViewModel() {
    private val repository = GumroadRepository()
    private val preferencesManager = PreferencesManager(context)
    private val payoutsCache = PayoutsCache(context)

    private val _uiState = MutableStateFlow<PayoutsUiState>(PayoutsUiState.Initial)
    val uiState: StateFlow<PayoutsUiState> = _uiState.asStateFlow()

    private val _payoutDetailsState = MutableStateFlow<PayoutDetailsState>(PayoutDetailsState.Idle)
    val payoutDetailsState: StateFlow<PayoutDetailsState> = _payoutDetailsState.asStateFlow()

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    private val _updateInterval = MutableStateFlow(UpdateInterval.NEVER)
    val updateInterval: StateFlow<UpdateInterval> = _updateInterval.asStateFlow()

    private var autoUpdateJob: Job? = null

    init {
        // Load saved preferences
        viewModelScope.launch {
            preferencesManager.accessToken.collect { token ->
                _accessToken.value = token
                if (token.isNotEmpty()) {
                    // Instantly load cached data first
                    val cachedData = payoutsCache.cachedPayouts.first()
                    if (cachedData.isNotEmpty()) {
                        _uiState.value = PayoutsUiState.Success(cachedData, isOfflineData = true)
                    }
                    // Then fetch fresh data in background
                    loadPayouts(silent = true)
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.updateInterval.collect { interval ->
                _updateInterval.value = interval
                setupAutoUpdate(interval)
            }
        }
    }

    fun setAccessToken(token: String) {
        viewModelScope.launch {
            preferencesManager.saveAccessToken(token)
        }
    }

    fun setUpdateInterval(interval: UpdateInterval) {
        viewModelScope.launch {
            preferencesManager.saveUpdateInterval(interval)
        }
    }

    fun clearAccessToken() {
        viewModelScope.launch {
            preferencesManager.clearAccessToken()
            payoutsCache.clearCache()
            _uiState.value = PayoutsUiState.Initial
        }
    }

    private fun setupAutoUpdate(interval: UpdateInterval) {
        autoUpdateJob?.cancel()

        if (interval != UpdateInterval.NEVER && interval.minutes != null) {
            autoUpdateJob = viewModelScope.launch {
                while (true) {
                    delay(interval.minutes * 60 * 1000) // Convert minutes to milliseconds
                    if (_accessToken.value.isNotEmpty()) {
                        loadPayouts(silent = true)
                    }
                }
            }
        }
    }

    fun loadPayouts(silent: Boolean = false) {
        if (_accessToken.value.isEmpty()) {
            _uiState.value = PayoutsUiState.Error("Please enter your access token")
            return
        }

        viewModelScope.launch {
            if (!silent) {
                _uiState.value = PayoutsUiState.Loading
            }
            val result = repository.getPayouts(_accessToken.value)
            result.fold(
                onSuccess = { response ->
                    // Save to cache
                    payoutsCache.savePayouts(response.payouts)
                    _uiState.value = PayoutsUiState.Success(response.payouts, isOfflineData = false)
                },
                onFailure = { error ->
                    // If we have cached data, show it with offline indicator
                    val cached = payoutsCache.cachedPayouts.first()
                    if (cached.isNotEmpty()) {
                        _uiState.value = PayoutsUiState.Success(cached, isOfflineData = true)
                    } else {
                        _uiState.value = PayoutsUiState.Error(error.message ?: "Unknown error occurred")
                    }
                }
            )
        }
    }

    fun loadPayoutDetails(payoutId: String) {
        viewModelScope.launch {
            _payoutDetailsState.value = PayoutDetailsState.Loading
            val result = repository.getPayoutDetails(payoutId, _accessToken.value)
            result.fold(
                onSuccess = { payout ->
                    _payoutDetailsState.value = PayoutDetailsState.Success(payout)
                },
                onFailure = { error ->
                    _payoutDetailsState.value = PayoutDetailsState.Error(error.message ?: "Failed to load details")
                }
            )
        }
    }

    fun clearPayoutDetails() {
        _payoutDetailsState.value = PayoutDetailsState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        autoUpdateJob?.cancel()
    }
}
