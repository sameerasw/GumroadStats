package com.sameerasw.gumroadstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.ui.components.cards.SaleCard
import com.sameerasw.gumroadstats.ui.components.sheets.SaleDetailsSheet
import com.sameerasw.gumroadstats.viewmodel.SaleDetailsState
import com.sameerasw.gumroadstats.viewmodel.SalesUiState
import com.sameerasw.gumroadstats.viewmodel.SalesViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val saleDetailsState by viewModel.saleDetailsState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Show bottom sheet when details are loaded
    LaunchedEffect(saleDetailsState) {
        if (saleDetailsState is SaleDetailsState.Success || saleDetailsState is SaleDetailsState.Loading) {
            showBottomSheet = true
        }
    }

    // Continuous haptic feedback during pull-to-refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            while (isRefreshing) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(100)
            }
        }
    }

    // Pagination Logic
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (uiState is SalesUiState.Success) {
                    val state = uiState as SalesUiState.Success
                    val totalItems = state.sales.size
                    if (lastIndex != null && lastIndex >= totalItems - 5 && state.nextPageKey != null) {
                         viewModel.loadSales(state.nextPageKey)
                    }
                }
            }
    }

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 150.dp

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is SalesUiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SalesUiState.Success -> {
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.loadSales() // Reload from scratch
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Stop haptic when refresh completes
                        LaunchedEffect(state) {
                             isRefreshing = false
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = topPadding,
                                bottom = bottomPadding,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(2.dp) // Consistent with PayoutsList
                        ) {
                             item {
                                 Text(
                                     text = stringResource(R.string.history),
                                     style = MaterialTheme.typography.titleMedium,
                                     modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                                 )
                             }

                             itemsIndexed(state.sales) { index, sale ->
                                 val isFirst = index == 0
                                 val isLast = index == state.sales.size - 1
                                 
                                 SaleCard(
                                     sale = sale,
                                     isFirst = isFirst,
                                     isLast = isLast,
                                     onClick = {
                                         haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                         viewModel.loadSaleDetails(sale.id)
                                     }
                                 )
                             }
                             
                             if (state.nextPageKey != null) {
                                 item {
                                     Box(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .padding(16.dp),
                                         contentAlignment = Alignment.Center
                                     ) {
                                         CircularProgressIndicator()
                                     }
                                 }
                             }
                        }
                    }
                }
                is SalesUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.loadSales()
                        }) {
                            Text("Retry")
                        }
                    }
                }
                SalesUiState.Loading -> {
                     Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Bottom Sheet for Sale Details
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.clearSaleDetails()
                },
                sheetState = sheetState
            ) {
                SaleDetailsSheet(
                    detailsState = saleDetailsState,
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.clearSaleDetails()
                    }
                )
            }
        }
    }
}
