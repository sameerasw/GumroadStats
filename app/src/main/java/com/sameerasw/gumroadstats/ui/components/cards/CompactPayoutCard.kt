package com.sameerasw.gumroadstats.ui.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.ui.components.common.StatusIconChip
import com.sameerasw.gumroadstats.utils.formatAmount
import com.sameerasw.gumroadstats.utils.formatDate

/**
 * Compact card component for displaying payout history items
 */
@Composable
fun CompactPayoutCard(
    payout: Payout,
    onClick: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val shape = when {
        isFirst && isLast -> MaterialTheme.shapes.medium // All corners rounded if single item
        isFirst -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        isLast -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
        else -> RoundedCornerShape(4.dp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                    text = "${formatAmount(payout.amount)} ${payout.currency.uppercase()}",
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

            StatusIconChip(status = payout.status)
        }
    }
}

