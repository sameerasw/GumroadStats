package com.sameerasw.gumroadstats.ui.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.data.model.User
import com.sameerasw.gumroadstats.utils.HapticUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * List component that displays all payouts with payable payout highlighted at top
 */
@Composable
fun PayoutsList(
    payouts: List<Payout>,
    user: User? = null,
    groupByMonth: Boolean = false,
    onPayoutClick: (Payout) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var expandedMonth by remember { mutableStateOf<String?>(null) }

    if (payouts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_payouts_found))
        }
    } else {
        // ... (separate payable and history payouts)
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

        // Calculate total collected amounts by currency
        val totalCollectedByCurrency = remember(payouts) {
            payouts
                .filter {
                    !it.status.equals("payable", ignoreCase = true) &&
                    !it.status.equals("failed", ignoreCase = true)
                }
                .groupBy { it.currency }
                .mapValues { (_, payoutsInCurrency) ->
                    val total = payoutsInCurrency.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                    val count = payoutsInCurrency.size
                    total to count
                }
        }

        // Monthly grouping logic
        val groupedPayouts = remember(historyPayouts, groupByMonth) {
            if (groupByMonth) {
                val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                
                historyPayouts.groupBy { payout ->
                    val date = parsePayoutDate(payout.createdAt)
                    if (date != null) sdfMonth.format(date) else "Unknown"
                }
            } else null
        }

        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Horizontal pager for cards at top
            item(key = "cards_carousel") {
                val payableCardCount = if (payablePayout != null) 1 else 0
                val userCardCount = if (user != null) 1 else 0
                val cardsCount = payableCardCount + totalCollectedByCurrency.size + userCardCount

                if (cardsCount > 0) {
                    val pagerState = rememberPagerState(pageCount = { cardsCount })

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 4.dp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) { page ->
                        when {
                            page == 0 && payablePayout != null -> {
                                PayablePayoutCard(
                                    payout = payablePayout,
                                    onClick = { onPayoutClick(payablePayout) }
                                )
                            }
                            page < payableCardCount + totalCollectedByCurrency.size -> {
                                val currencyIndex = page - payableCardCount
                                val currencyEntry = totalCollectedByCurrency.entries.elementAtOrNull(currencyIndex)

                                currencyEntry?.let { entry ->
                                    val (total, count) = entry.value
                                    TotalCollectedCard(
                                        totalAmount = total,
                                        currency = entry.key,
                                        payoutCount = count,
                                        onClick = { }
                                    )
                                }
                            }
                            else -> {
                                user?.let { userData ->
                                    UserInfoCard(user = userData, onClick = { })
                                }
                            }
                        }
                    }
                }
            }

            // History section
            if (historyPayouts.isNotEmpty()) {
                if (groupByMonth && groupedPayouts != null) {
                    val groupKeys = groupedPayouts.keys.toList()
                    val totalGroups = groupKeys.size

                    groupKeys.forEachIndexed { groupIndex, month ->
                        val payoutsInMonth = groupedPayouts[month] ?: emptyList()
                        val isExpanded = expandedMonth == month
                        val isFirstGroup = groupIndex == 0
                        val isLastGroup = groupIndex == totalGroups - 1
                        
                        // Monthly Header
                        item(key = "header_$month") {
                            val totalAmount = payoutsInMonth.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                            val currency = payoutsInMonth.firstOrNull()?.currency ?: ""
                            
                            val headerColor = if (isExpanded) {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }

                            MonthlyGroupHeader(
                                month = month,
                                totalAmount = totalAmount,
                                currency = currency,
                                count = payoutsInMonth.size,
                                isExpanded = isExpanded,
                                isFirst = isFirstGroup,
                                isLast = isLastGroup && !isExpanded,
                                containerColor = headerColor,
                                onClick = {
                                    HapticUtil.performClick(haptic)
                                    expandedMonth = if (isExpanded) null else month
                                },
                                modifier = Modifier.animateItem()
                            )
                        }

                        if (isExpanded) {
                            itemsIndexed(
                                items = payoutsInMonth,
                                key = { _, payout -> payout.id ?: payout.createdAt }
                            ) { index, payout ->
                                val isLastPayoutOverall = isLastGroup && index == payoutsInMonth.size - 1
                                CompactPayoutCard(
                                    payout = payout,
                                    onClick = { onPayoutClick(payout) },
                                    modifier = Modifier.padding(horizontal = 16.dp).animateItem(),
                                    isFirst = false,
                                    isLast = isLastPayoutOverall,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                )
                            }
                        }
                    }
                } else {
                    item(key = "history_header") {
                        Text(
                            text = stringResource(R.string.history),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
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
                            modifier = Modifier.padding(horizontal = 16.dp),
                            isFirst = isFirst,
                            isLast = isLast
                        )
                    }
                }
            }
        }
    }
}

/**
 * Robust date parser for Gumroad dates
 */
private fun parsePayoutDate(dateString: String): Date? {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd"
    )
    
    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.US)
            if (format.contains("'Z'")) {
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(dateString)
            if (date != null) return date
        } catch (e: Exception) {
            // Try next format
        }
    }
    return null
}

@Composable
fun MonthlyGroupHeader(
    month: String,
    totalAmount: Double,
    currency: String,
    count: Int,
    isExpanded: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = when {
        isFirst && isLast -> MaterialTheme.shapes.medium
        isFirst -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        isLast -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        else -> RoundedCornerShape(4.dp)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${String.format(Locale.US, "%,.2f", totalAmount)} ${currency.uppercase()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$month • $count payouts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                painter = painterResource(id = R.drawable.rounded_expand_circle_right_24),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer(rotationZ = if (isExpanded) 90f else 0f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
