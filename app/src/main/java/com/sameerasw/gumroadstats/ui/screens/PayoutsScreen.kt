package com.sameerasw.gumroadstats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.viewmodel.PayoutsUiState
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel
import com.sameerasw.gumroadstats.viewmodel.PayoutDetailsState
import java.text.SimpleDateFormat
import java.util.*

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

    // Show bottom sheet when details are loaded
    LaunchedEffect(payoutDetailsState) {
        showBottomSheet = payoutDetailsState is PayoutDetailsState.Success ||
                         payoutDetailsState is PayoutDetailsState.Loading
    }

    Scaffold(
        topBar = {
            if (accessToken.isNotEmpty()) {
                TopAppBar(
                    title = { Text("Gumroad Payouts") },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
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
                // Access Token Input Screen - properly handle edge-to-edge
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
                        onValueChange = { tokenInput = it },
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
                                    viewModel.setAccessToken(tokenInput)
                                }
                            }
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (tokenInput.isNotEmpty()) {
                                viewModel.setAccessToken(tokenInput)
                            }
                        },
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
            } else {
                // Payouts Display Screen
                when (val state = uiState) {
                    is PayoutsUiState.Initial -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // LoadingIndicator removed
                        }
                    }
                    is PayoutsUiState.Success -> {
                        PullToRefreshBox(
                            isRefreshing = state.isOfflineData,
                            onRefresh = { viewModel.loadPayouts() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            PayoutsList(
                                payouts = state.payouts,
                                isOfflineData = state.isOfflineData,
                                onPayoutClick = { payout ->
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
                            Button(onClick = { viewModel.loadPayouts() }) {
                                Text("Retry")
                            }
                        }
                    }

                    PayoutsUiState.Loading -> TODO()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutsList(
    payouts: List<Payout>,
    isOfflineData: Boolean = false,
    onPayoutClick: (Payout) -> Unit
) {
    if (payouts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No payouts found")
        }
    } else {
        // Separate payable payout (if exists) from the rest
        val payablePayout = payouts.firstOrNull { it.status.equals("payable", ignoreCase = true) }
        val historyPayouts = if (payablePayout != null) {
            payouts.filter { it != payablePayout }
        } else {
            payouts
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Payable card at top with primary styling
            if (payablePayout != null) {
                item {
                    PayablePayoutCard(
                        payout = payablePayout,
                        onClick = { onPayoutClick(payablePayout) }
                    )
                }
            }

            // History section
            if (historyPayouts.isNotEmpty()) {
                item {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                items(historyPayouts.size) { index ->
                    val payout = historyPayouts[index]
                    val isFirst = index == 0
                    val isLast = index == historyPayouts.size - 1

                    CompactPayoutCard(
                        payout = payout,
                        onClick = { onPayoutClick(payout) },
                        isFirst = isFirst,
                        isLast = isLast
                    )
                }
            }
        }
    }
}

@Composable
fun PayablePayoutCard(
    payout: Payout,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Available Payout",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${payout.amount} ${payout.currency}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(payout.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                StatusChip(status = payout.status)
            }
        }
    }
}

@Composable
fun CompactPayoutCard(
    payout: Payout,
    onClick: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val shape = when {
        isFirst && isLast -> MaterialTheme.shapes.medium // All corners rounded if single item
        isFirst -> androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        isLast -> androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
        else -> androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${payout.amount} ${payout.currency}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(payout.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusChip(status = payout.status)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutDetailsSheet(
    detailsState: PayoutDetailsState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payout Details",
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        HorizontalDivider()

        when (detailsState) {
            is PayoutDetailsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // LoadingIndicator removed
                }
            }
            is PayoutDetailsState.Success -> {
                PayoutDetailsContent(payout = detailsState.payout)
            }
            is PayoutDetailsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${detailsState.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun PayoutDetailsContent(payout: Payout) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount and Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${payout.amount} ${payout.currency}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            StatusChip(status = payout.status)
        }

        HorizontalDivider()

        // Payment Details
        DetailRow(label = "Payment Processor", value = payout.paymentProcessor.uppercase())

        if (payout.bankAccountVisual != null) {
            DetailRow(label = "Bank Account", value = payout.bankAccountVisual)
        }

        if (payout.paypalEmail != null) {
            DetailRow(label = "PayPal Email", value = payout.paypalEmail)
        }

        HorizontalDivider()

        // Dates
        DetailRow(label = "Created", value = formatDate(payout.createdAt))

        if (payout.processedAt != null) {
            DetailRow(label = "Processed", value = formatDate(payout.processedAt))
        }

        if (payout.id != null) {
            HorizontalDivider()
            DetailRow(label = "Payout ID", value = payout.id)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "completed" -> MaterialTheme.colorScheme.primary
        "pending" -> MaterialTheme.colorScheme.tertiary
        "payable" -> MaterialTheme.colorScheme.secondary
        "failed" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
