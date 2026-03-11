import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.data.model.User
import com.sameerasw.gumroadstats.ui.components.cards.CompactPayoutCard
import com.sameerasw.gumroadstats.ui.components.cards.PayablePayoutCard
import com.sameerasw.gumroadstats.ui.components.cards.TotalCollectedCard
import com.sameerasw.gumroadstats.ui.components.cards.UserInfoCard

/**
 * List component that displays all payouts with payable payout highlighted at top
 */
@Composable
fun PayoutsList(
    payouts: List<Payout>,
    user: User? = null,
    onPayoutClick: (Payout) -> Unit
) {
    if (payouts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_payouts_found))
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

        // Calculate total collected amounts by currency
        // Exclude payable and failed statuses, group by currency
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
                // Count cards: payable payout (if available) + total collected cards + user (if available)
                val payableCardCount = if (payablePayout != null) 1 else 0
                val userCardCount = if (user != null) 1 else 0
                val cardsCount = payableCardCount + totalCollectedByCurrency.size + userCardCount

                if (cardsCount > 0) {
                    val pagerState = rememberPagerState(pageCount = { cardsCount })

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        when {
                            // First page: Payable payout card (if exists)
                            page == 0 && payablePayout != null -> {
                                PayablePayoutCard(
                                    payout = payablePayout,
                                    onClick = { onPayoutClick(payablePayout) }
                                )
                            }
                            // Middle pages: Total collected cards for each currency
                            page < payableCardCount + totalCollectedByCurrency.size -> {
                                val currencyIndex = page - payableCardCount
                                val currencyEntry = totalCollectedByCurrency.entries.elementAtOrNull(currencyIndex)

                                currencyEntry?.let { entry ->
                                    val currency = entry.key
                                    val (total, count) = entry.value
                                    TotalCollectedCard(
                                        totalAmount = total,
                                        currency = currency,
                                        payoutCount = count,
                                        onClick = { }
                                    )
                                }
                            }
                            // Last page: User info card (if user is available)
                            else -> {
                                user?.let { userData ->
                                    UserInfoCard(
                                        user = userData,
                                        onClick = { }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // History section
            if (historyPayouts.isNotEmpty()) {
                item(key = "history_header") {
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                itemsIndexed(
                    items = historyPayouts,
                    key = { _, payout -> (payout as Payout).id ?: payout.createdAt }
                ) { index: Int, payout: Payout ->
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
