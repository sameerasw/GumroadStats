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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.sameerasw.gumroadstats.viewmodel.SalesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    payoutsViewModel: PayoutsViewModel,
    salesViewModel: SalesViewModel,
    onNavigateToSettings: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.Bottom)

    val tabs = listOf(
        AirSyncTab("Payouts", Icons.Default.AttachMoney, 0),
        AirSyncTab("Sales", Icons.Default.ShoppingCart, 1)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Text(if (pagerState.currentPage == 0) "Gumroad Payouts" else "Sales") 
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToSettings()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
