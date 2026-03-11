package com.sameerasw.gumroadstats.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sameerasw.gumroadstats.data.local.PayoutsCache
import com.sameerasw.gumroadstats.data.local.UserCache
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.data.model.User
import com.sameerasw.gumroadstats.data.preferences.PreferencesManager
import com.sameerasw.gumroadstats.data.preferences.UpdateInterval
import com.sameerasw.gumroadstats.data.repository.GumroadRepository
import com.sameerasw.gumroadstats.widget.WidgetUpdateHelper
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

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}

class PayoutsViewModel(private val context: Context) : ViewModel() {
    private val repository = GumroadRepository()
    private val preferencesManager = PreferencesManager(context)
    private val payoutsCache = PayoutsCache.getInstance(context)
    private val userCache = UserCache.getInstance(context)

    private val _uiState = MutableStateFlow<PayoutsUiState>(PayoutsUiState.Initial)
    val uiState: StateFlow<PayoutsUiState> = _uiState.asStateFlow()

    private val _payoutDetailsState = MutableStateFlow<PayoutDetailsState>(PayoutDetailsState.Idle)
    val payoutDetailsState: StateFlow<PayoutDetailsState> = _payoutDetailsState.asStateFlow()

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    private val _updateInterval = MutableStateFlow(UpdateInterval.NEVER)
    val updateInterval: StateFlow<UpdateInterval> = _updateInterval.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _groupByMonth = MutableStateFlow(false)
    val groupByMonth: StateFlow<Boolean> = _groupByMonth.asStateFlow()

    private var loadPayoutsJob: Job? = null
    private var autoUpdateJob: Job? = null

    init {
        // Load saved preferences
        viewModelScope.launch {
            preferencesManager.accessToken.collect { token ->
                _accessToken.value = token
                if (token.isNotEmpty()) {
                    // Instantly load cached payouts data first
                    val cachedData = payoutsCache.cachedPayouts.first()
                    if (cachedData.isNotEmpty()) {
                        _uiState.value = PayoutsUiState.Success(cachedData, isOfflineData = true)
                    } else {
                        // Show loading state only if there's no cached data
                        _uiState.value = PayoutsUiState.Loading
                    }
                    // Then fetch fresh payouts data in background
                    loadPayouts(silent = cachedData.isNotEmpty())

                    // Load cached user data (don't fetch fresh on init)
                    val cachedUser = userCache.cachedUser.first()
                    if (cachedUser != null) {
                        _userState.value = UserState.Success(cachedUser)
                    }
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.updateInterval.collect { interval ->
                _updateInterval.value = interval
                setupAutoUpdate(interval)
            }
        }

        viewModelScope.launch {
            preferencesManager.startDate.collect { date ->
                val oldDate = _startDate.value
                _startDate.value = date
                // Only reload if the date actually changed and we have a token
                if (_accessToken.value.isNotEmpty() && oldDate != null && oldDate != date) {
                    loadPayouts(silent = false)
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.groupByMonth.collect { enabled ->
                _groupByMonth.value = enabled
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

    fun setStartDate(date: Long) {
        viewModelScope.launch {
            preferencesManager.saveStartDate(date)
        }
    }

    fun clearStartDate() {
        viewModelScope.launch {
            preferencesManager.clearStartDate()
        }
    }

    fun setGroupByMonth(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveGroupByMonth(enabled)
        }
    }

    fun clearAccessToken() {
        viewModelScope.launch {
            preferencesManager.clearAccessToken()
            payoutsCache.clearCache()
            userCache.clearCache()
            _uiState.value = PayoutsUiState.Initial
            _userState.value = UserState.Idle
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
        loadPayoutsJob?.cancel()

        if (_accessToken.value.isEmpty()) {
            _uiState.value = PayoutsUiState.Error("Please enter your access token")
            return
        }

        loadPayoutsJob = viewModelScope.launch {
            try {
                if (!silent) {
                    _uiState.value = PayoutsUiState.Loading
                }

                val allPayouts = mutableListOf<Payout>()
                var nextPageKey: String? = null
                val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val afterDate = _startDate.value?.let { dateFormatter.format(java.util.Date(it)) }

                do {
                    val result = repository.getPayouts(
                        accessToken = _accessToken.value,
                        after = afterDate,
                        pageKey = nextPageKey
                    )
                    
                    var shouldContinue = false
                    result.fold(
                        onSuccess = { response ->
                            allPayouts.addAll(response.payouts)
                            nextPageKey = response.nextPageKey
                            shouldContinue = nextPageKey != null
                        },
                        onFailure = { error ->
                            throw error
                        }
                    )
                } while (shouldContinue)
                
                // Save to cache
                payoutsCache.savePayouts(allPayouts)
                _uiState.value = PayoutsUiState.Success(allPayouts, isOfflineData = false)

                // Update widgets when data changes
                WidgetUpdateHelper.updateAllWidgets(context)

            } catch (e: Exception) {
                // Ensure we always update the UI state even if something unexpected happens
                val cached = payoutsCache.cachedPayouts.first()
                if (cached.isNotEmpty()) {
                    _uiState.value = PayoutsUiState.Success(cached, isOfflineData = true)
                } else {
                    _uiState.value = PayoutsUiState.Error("An unexpected error occurred: ${e.message}")
                }
            }
        }
    }

    fun loadPayoutDetails(payoutId: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _payoutDetailsState.value = PayoutDetailsState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun clearPayoutDetails() {
        _payoutDetailsState.value = PayoutDetailsState.Idle
    }

    fun loadUser(silent: Boolean = false) {
        if (_accessToken.value.isEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                if (!silent) {
                    _userState.value = UserState.Loading
                }
                val result = repository.getUser(_accessToken.value)
                result.fold(
                    onSuccess = { user ->
                        // Save to cache
                        userCache.saveUser(user)
                        _userState.value = UserState.Success(user)
                    },
                    onFailure = { error ->
                        // If we have cached data, show it
                        val cached = userCache.cachedUser.first()
                        if (cached != null) {
                            _userState.value = UserState.Success(cached)
                        } else {
                            _userState.value = UserState.Error(error.message ?: "Failed to load user data")
                        }
                    }
                )
            } catch (e: Exception) {
                // Fallback to cached data if available
                val cached = userCache.cachedUser.first()
                if (cached != null) {
                    _userState.value = UserState.Success(cached)
                } else {
                    _userState.value = UserState.Error("An unexpected error occurred")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoUpdateJob?.cancel()
    }
}
