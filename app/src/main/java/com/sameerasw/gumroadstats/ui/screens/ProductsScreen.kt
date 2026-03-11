package com.sameerasw.gumroadstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.ui.components.cards.ProductCard
import com.sameerasw.gumroadstats.ui.components.sheets.ProductDetailsSheet
import com.sameerasw.gumroadstats.viewmodel.ProductsUiState
import com.sameerasw.gumroadstats.viewmodel.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val detailsState by viewModel.productDetailsState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    
    var showDetailsSheet by remember { mutableStateOf(false) }

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 150.dp

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is ProductsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProductsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadProducts() }) {
                                 Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                is ProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text(stringResource(R.string.no_products_found))
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = topPadding,
                                bottom = bottomPadding,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            itemsIndexed(state.products) { index, product ->
                                val isFirst = index == 0
                                val isLast = index == state.products.size - 1
                                
                                ProductCard(
                                    product = product,
                                    isFirst = isFirst,
                                    isLast = isLast,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.loadProductDetails(product.id)
                                        showDetailsSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showDetailsSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDetailsSheet = false
                    viewModel.clearProductDetails()
                }
            ) {
                ProductDetailsSheet(
                    detailsState = detailsState,
                    onToggleStatus = { product ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleProductStatus(product)
                    },
                    onDelete = { productId ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteProduct(productId)
                        showDetailsSheet = false
                    },
                    onDismiss = {
                        showDetailsSheet = false
                        viewModel.clearProductDetails()
                    }
                )
            }
        }
    }
}
