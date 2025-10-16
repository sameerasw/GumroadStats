package com.sameerasw.gumroadstats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
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
                    title = {
                        Column {
                            Text("Gumroad Payouts")
                            if (uiState is PayoutsUiState.Success && (uiState as PayoutsUiState.Success).isOfflineData) {
                                Text(
                                    "Offline Mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        if (uiState is PayoutsUiState.Success && (uiState as PayoutsUiState.Success).isOfflineData) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Offline",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.loadPayouts() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
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
                            CircularProgressIndicator()
                        }
                    }
                    is PayoutsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is PayoutsUiState.Success -> {
                        PayoutsList(
                            payouts = state.payouts,
                            isOfflineData = state.isOfflineData,
                            onPayoutClick = { payout ->
                                payout.id?.let { viewModel.loadPayoutDetails(it) }
                            }
                        )
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isOfflineData) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Showing cached data. Tap refresh for latest updates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(payouts) { payout ->
                CompactPayoutCard(
                    payout = payout,
                    onClick = { onPayoutClick(payout) }
                )
            }
        }
    }
}

@Composable
fun CompactPayoutCard(
    payout: Payout,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

        Divider()

        when (detailsState) {
            is PayoutDetailsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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

        Divider()

        // Payment Details
        DetailRow(label = "Payment Processor", value = payout.paymentProcessor.uppercase())

        if (payout.bankAccountVisual != null) {
            DetailRow(label = "Bank Account", value = payout.bankAccountVisual)
        }

        if (payout.paypalEmail != null) {
            DetailRow(label = "PayPal Email", value = payout.paypalEmail)
        }

        Divider()

        // Dates
        DetailRow(label = "Created", value = formatDate(payout.createdAt))

        if (payout.processedAt != null) {
            DetailRow(label = "Processed", value = formatDate(payout.processedAt))
        }

        if (payout.id != null) {
            Divider()
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
