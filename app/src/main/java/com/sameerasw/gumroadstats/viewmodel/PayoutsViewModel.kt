package com.sameerasw.gumroadstats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.data.repository.GumroadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PayoutsUiState {
    object Initial : PayoutsUiState()
    object Loading : PayoutsUiState()
    data class Success(val payouts: List<Payout>) : PayoutsUiState()
    data class Error(val message: String) : PayoutsUiState()
}

class PayoutsViewModel : ViewModel() {
    private val repository = GumroadRepository()

    private val _uiState = MutableStateFlow<PayoutsUiState>(PayoutsUiState.Initial)
    val uiState: StateFlow<PayoutsUiState> = _uiState.asStateFlow()

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    fun setAccessToken(token: String) {
        _accessToken.value = token
    }

    fun loadPayouts() {
        if (_accessToken.value.isEmpty()) {
            _uiState.value = PayoutsUiState.Error("Please enter your access token")
            return
        }

        viewModelScope.launch {
            _uiState.value = PayoutsUiState.Loading
            val result = repository.getPayouts(_accessToken.value)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = PayoutsUiState.Success(response.payouts)
                },
                onFailure = { error ->
                    _uiState.value = PayoutsUiState.Error(error.message ?: "Unknown error occurred")
                }
            )
        }
    }
}
