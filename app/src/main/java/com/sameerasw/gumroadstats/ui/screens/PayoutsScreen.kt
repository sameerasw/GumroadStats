package com.sameerasw.gumroadstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.ui.components.cards.PayoutsList
import com.sameerasw.gumroadstats.ui.components.sheets.PayoutDetailsSheet
import com.sameerasw.gumroadstats.viewmodel.PayoutDetailsState
import com.sameerasw.gumroadstats.viewmodel.PayoutsUiState
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main screen for displaying Gumroad payouts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutsScreen(
    viewModel: PayoutsViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val accessToken by viewModel.accessToken.collectAsState()
    val payoutDetailsState by viewModel.payoutDetailsState.collectAsState()
    var tokenInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    var isRefreshing by remember { mutableStateOf(false) }

    // Show bottom sheet when details are loaded
    LaunchedEffect(payoutDetailsState) {
        showBottomSheet = payoutDetailsState is PayoutDetailsState.Success ||
                         payoutDetailsState is PayoutDetailsState.Loading
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

    Scaffold(
        topBar = {
            if (accessToken.isNotEmpty()) {
                TopAppBar(
                    title = { Text("Gumroad Payouts") },
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
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (accessToken.isEmpty()) {
                // Access Token Input Screen
                AccessTokenInputScreen(
                    tokenInput = tokenInput,
                    onTokenInputChange = { tokenInput = it },
                    onSaveToken = {
                        if (tokenInput.isNotEmpty()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setAccessToken(tokenInput)
                        }
                    },
                    keyboardController = keyboardController
                )
            } else {
                // Payouts Display Screen
                when (val state = uiState) {
                    is PayoutsUiState.Initial -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is PayoutsUiState.Success -> {
                        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                            isRefreshing = state.isOfflineData,
                            onRefresh = {
                                isRefreshing = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.loadPayouts()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Stop haptic when refresh completes
                            LaunchedEffect(state.isOfflineData) {
                                if (!state.isOfflineData) {
                                    isRefreshing = false
                                }
                            }

                            PayoutsList(
                                payouts = state.payouts,
                                onPayoutClick = { payout ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    payout.id?.let { viewModel.loadPayoutDetails(it) }
                                }
                            )
                        }
                    }
                    is PayoutsUiState.Error -> {
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
                                viewModel.loadPayouts()
                            }) {
                                Text("Retry")
                            }
                        }
                    }

                    PayoutsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        // Bottom Sheet for Payout Details
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.clearPayoutDetails()
                },
                sheetState = sheetState
            ) {
                PayoutDetailsSheet(
                    detailsState = payoutDetailsState,
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.clearPayoutDetails()
                    }
                )
            }
        }
    }
}

@Composable
private fun AccessTokenInputScreen(
    tokenInput: String,
    onTokenInputChange: (String) -> Unit,
    onSaveToken: () -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Gumroad Stats",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enter your Gumroad Access Token",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = tokenInput,
            onValueChange = onTokenInputChange,
            label = { Text("Access Token") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (tokenInput.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveToken()
                    }
                }
            ),
            singleLine = true
        )

        Button(
            onClick = onSaveToken,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save and Load Payouts")
        }

        Text(
            text = "You can generate your access token from your Gumroad application settings with 'view_payouts' scope",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
