package com.sameerasw.gumroadstats.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.ui.components.common.DetailRow
import com.sameerasw.gumroadstats.ui.components.common.StatusChip
import com.sameerasw.gumroadstats.utils.formatAmount
import com.sameerasw.gumroadstats.utils.formatDate
import com.sameerasw.gumroadstats.viewmodel.PayoutDetailsState

/**
 * Bottom sheet that displays detailed information about a payout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutDetailsSheet(
    detailsState: PayoutDetailsState,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

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
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) {
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
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading payout details...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
private fun PayoutDetailsContent(payout: Payout) {
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
                    text = "${formatAmount(payout.amount)} ${payout.currency.uppercase()}",
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
