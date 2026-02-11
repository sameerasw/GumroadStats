package com.sameerasw.gumroadstats.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sameerasw.gumroadstats.data.model.Product
import com.sameerasw.gumroadstats.data.preferences.PreferencesManager
import com.sameerasw.gumroadstats.data.repository.GumroadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductsUiState {
    data object Initial : ProductsUiState()
    data object Loading : ProductsUiState()
    data class Success(val products: List<Product>) : ProductsUiState()
    data class Error(val message: String) : ProductsUiState()
}

sealed class ProductDetailsState {
    data object Initial : ProductDetailsState()
    data object Loading : ProductDetailsState()
    data class Success(val product: Product) : ProductDetailsState()
    data class Error(val message: String) : ProductDetailsState()
}

class ProductsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GumroadRepository()
    private val preferencesManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Initial)
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val _productDetailsState = MutableStateFlow<ProductDetailsState>(ProductDetailsState.Initial)
    val productDetailsState: StateFlow<ProductDetailsState> = _productDetailsState.asStateFlow()

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.accessToken.collect { token ->
                _accessToken.value = token ?: ""
                if (!token.isNullOrEmpty()) {
                    loadProducts()
                }
            }
        }
    }

    fun loadProducts() {
        if (_accessToken.value.isEmpty()) return

        _uiState.value = ProductsUiState.Loading
        viewModelScope.launch {
            val result = repository.getProducts(_accessToken.value)

            result.onSuccess { response ->
                _uiState.value = ProductsUiState.Success(response.products)
            }.onFailure { error ->
                _uiState.value = ProductsUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun loadProductDetails(productId: String) {
        if (_accessToken.value.isEmpty()) return

        _productDetailsState.value = ProductDetailsState.Loading
        viewModelScope.launch {
            val result = repository.getProductDetails(productId, _accessToken.value)

            result.onSuccess { product ->
                _productDetailsState.value = ProductDetailsState.Success(product)
            }.onFailure { error ->
                _productDetailsState.value = ProductDetailsState.Error(error.message ?: "Failed to load details")
            }
        }
    }

    fun toggleProductStatus(product: Product) {
        if (_accessToken.value.isEmpty()) return

        viewModelScope.launch {
            val result = if (product.published) {
                repository.disableProduct(product.id, _accessToken.value)
            } else {
                repository.enableProduct(product.id, _accessToken.value)
            }

            result.onSuccess { updatedProduct ->
                // Update listing state
                val currentState = _uiState.value
                if (currentState is ProductsUiState.Success) {
                    val updatedList = currentState.products.map {
                        if (it.id == updatedProduct.id) updatedProduct else it
                    }
                    _uiState.value = ProductsUiState.Success(updatedList)
                }
                
                // Update details state if open
                val currentDetails = _productDetailsState.value
                if (currentDetails is ProductDetailsState.Success && currentDetails.product.id == updatedProduct.id) {
                    _productDetailsState.value = ProductDetailsState.Success(updatedProduct)
                }
            }
        }
    }

    fun deleteProduct(productId: String) {
        if (_accessToken.value.isEmpty()) return

        viewModelScope.launch {
            val result = repository.deleteProduct(productId, _accessToken.value)

            result.onSuccess {
                // Remove from listing
                val currentState = _uiState.value
                if (currentState is ProductsUiState.Success) {
                    val updatedList = currentState.products.filter { it.id != productId }
                    _uiState.value = ProductsUiState.Success(updatedList)
                }
                _productDetailsState.value = ProductDetailsState.Initial
            }
        }
    }

    fun clearProductDetails() {
        _productDetailsState.value = ProductDetailsState.Initial
    }
}
