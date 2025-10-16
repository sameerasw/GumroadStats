package com.sameerasw.gumroadstats.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.data.model.Payout

/**
 * List component that displays all payouts with payable payout highlighted at top
 */
@Composable
fun PayoutsList(
    payouts: List<Payout>,
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
        val payablePayout = remember(payouts) {
            payouts.firstOrNull { it.status.equals("payable", ignoreCase = true) }
        }
        val historyPayouts = remember(payouts, payablePayout) {
            if (payablePayout != null) {
                payouts.filter { it != payablePayout }
            } else {
                payouts
            }
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
                item(key = "payable_${payablePayout.id}") {
                    PayablePayoutCard(
                        payout = payablePayout,
                        onClick = { onPayoutClick(payablePayout) }
                    )
                }
            }

            // History section
            if (historyPayouts.isNotEmpty()) {
                item(key = "history_header") {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                itemsIndexed(
                    items = historyPayouts,
                    key = { _, payout -> payout.id ?: payout.createdAt }
                ) { index, payout ->
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

