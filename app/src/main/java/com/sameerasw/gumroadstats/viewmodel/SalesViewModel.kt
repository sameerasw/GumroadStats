package com.sameerasw.gumroadstats.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sameerasw.gumroadstats.data.model.Sale
import com.sameerasw.gumroadstats.data.preferences.PreferencesManager
import com.sameerasw.gumroadstats.data.repository.GumroadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SalesUiState {
    data object Initial : SalesUiState()
    data object Loading : SalesUiState()
    data class Success(val sales: List<Sale>, val isOfflineData: Boolean = false, val nextPageKey: String? = null) : SalesUiState()
    data class Error(val message: String) : SalesUiState()
}

sealed class SaleDetailsState {
    data object Initial : SaleDetailsState()
    data object Loading : SaleDetailsState()
    data class Success(val sale: Sale) : SaleDetailsState()
    data class Error(val message: String) : SaleDetailsState()
}

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GumroadRepository()
    private val preferencesManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow<SalesUiState>(SalesUiState.Initial)
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _saleDetailsState = MutableStateFlow<SaleDetailsState>(SaleDetailsState.Initial)
    val saleDetailsState: StateFlow<SaleDetailsState> = _saleDetailsState.asStateFlow()

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.accessToken.collect { token ->
                _accessToken.value = token ?: ""
                if (!token.isNullOrEmpty()) {
                    loadSales()
                }
            }
        }
    }

    fun loadSales(pageKey: String? = null) {
        if (_accessToken.value.isEmpty()) return

        if (pageKey == null) {
             _uiState.value = SalesUiState.Loading
        }

        viewModelScope.launch {
            val result = repository.getSales(
                accessToken = _accessToken.value,
                pageKey = pageKey
            )

            result.onSuccess { response ->
                val currentSales = if (pageKey != null && _uiState.value is SalesUiState.Success) {
                    (_uiState.value as SalesUiState.Success).sales
                } else {
                    emptyList()
                }
                
                _uiState.value = SalesUiState.Success(
                    sales = currentSales + response.sales,
                    isOfflineData = false,
                    nextPageKey = response.nextPageKey
                )
            }.onFailure { error ->
                if (pageKey == null) {
                    _uiState.value = SalesUiState.Error(error.message ?: "Unknown error")
                } else {
                    // Handle pagination error silently or show toast via side effect
                     // For now, keep existing state
                }
            }
        }
    }

    fun loadSaleDetails(saleId: String) {
        if (_accessToken.value.isEmpty()) return

        _saleDetailsState.value = SaleDetailsState.Loading
        viewModelScope.launch {
            val result = repository.getSaleDetails(saleId, _accessToken.value)
            
            result.onSuccess { sale ->
                _saleDetailsState.value = SaleDetailsState.Success(sale)
            }.onFailure { error ->
                _saleDetailsState.value = SaleDetailsState.Error(error.message ?: "Failed to load details")
            }
        }
    }

    fun clearSaleDetails() {
        _saleDetailsState.value = SaleDetailsState.Initial
    }
}
