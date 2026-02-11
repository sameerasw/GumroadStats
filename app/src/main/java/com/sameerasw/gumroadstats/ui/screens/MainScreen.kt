package com.sameerasw.gumroadstats.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import com.sameerasw.gumroadstats.viewmodel.ProductsViewModel
import com.sameerasw.gumroadstats.ui.screens.ProductsScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sameerasw.gumroadstats.ui.components.AirSyncFloatingToolbar
import com.sameerasw.gumroadstats.ui.model.AirSyncTab
import com.sameerasw.gumroadstats.utils.HapticUtil
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel
import androidx.compose.ui.res.stringResource
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.viewmodel.SalesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    payoutsViewModel: PayoutsViewModel,
    salesViewModel: SalesViewModel,
    productsViewModel: ProductsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    val tabPayouts = stringResource(R.string.tab_payouts)
    val tabSales = stringResource(R.string.tab_sales)
    val tabInventory = stringResource(R.string.tab_inventory)
    
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    val tabs = remember(tabPayouts, tabSales, tabInventory) {
        listOf(
            AirSyncTab(tabPayouts, Icons.Default.AttachMoney, 0),
            AirSyncTab(tabSales, Icons.Default.ShoppingCart, 1),
            AirSyncTab(tabInventory, Icons.Default.Inventory, 2)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        when (pagerState.currentPage) {
                            0 -> stringResource(R.string.tab_payouts)
                            1 -> stringResource(R.string.tab_sales)
                            else -> stringResource(R.string.tab_inventory)
                        }
                    )
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToSettings()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> PayoutsScreen(
                        viewModel = payoutsViewModel,
                        onNavigateToSettings = onNavigateToSettings,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> SalesScreen(
                        viewModel = salesViewModel,
                        onNavigateToSettings = onNavigateToSettings,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> ProductsScreen(
                        viewModel = productsViewModel,
                        onNavigateToSettings = onNavigateToSettings,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            AirSyncFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -12.dp)
                    .zIndex(1f),
                currentPage = pagerState.currentPage,
                tabs = tabs,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    }

    // Haptic feedback on page change
    LaunchedEffect(pagerState.currentPage) {
        HapticUtil.performLightTick(haptic)
    }
}
