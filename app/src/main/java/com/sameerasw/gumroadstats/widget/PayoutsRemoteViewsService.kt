package com.sameerasw.gumroadstats.widget

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.local.PayoutsCache
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.utils.formatAmount
import com.sameerasw.gumroadstats.utils.formatDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Service that provides the factory for RemoteViews
 */
class PayoutsRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return PayoutsRemoteViewsFactory(this.applicationContext)
    }
}

/**
 * Factory that creates RemoteViews for each payout item
 */
class PayoutsRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var payouts: List<Payout> = emptyList()
    private val cache = PayoutsCache(context)

    override fun onCreate() {
        loadPayouts()
    }

    override fun onDataSetChanged() {
        loadPayouts()
    }

    private fun loadPayouts() {
        runBlocking {
            val cachedPayouts = cache.cachedPayouts.first()
            if (cachedPayouts.isNotEmpty()) {
                // Separate payable payout and put it first
                val payablePayout = cachedPayouts.firstOrNull {
                    it.status.equals("payable", ignoreCase = true)
                }
                val otherPayouts = cachedPayouts.filter {
                    !it.status.equals("payable", ignoreCase = true)
                }

                payouts = if (payablePayout != null) {
                    listOf(payablePayout) + otherPayouts
                } else {
                    cachedPayouts
                }
            }
        }
    }

    override fun onDestroy() {
        payouts = emptyList()
    }

    override fun getCount(): Int = payouts.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= payouts.size) {
            return RemoteViews(context.packageName, R.layout.widget_payout_item)
        }

        val payout = payouts[position]
        val isPayable = payout.status.equals("payable", ignoreCase = true)

        // Use different layout for payable payout
        val layoutId = if (isPayable) {
            R.layout.widget_payout_item_featured
        } else {
            R.layout.widget_payout_item
        }

        val views = RemoteViews(context.packageName, layoutId)

        // Set the data
        views.setTextViewText(
            R.id.payout_amount,
            "${formatAmount(payout.amount)} ${payout.currency.uppercase()}"
        )
        views.setTextViewText(
            R.id.payout_date,
            formatDate(payout.createdAt)
        )

        // Set status icon based on status type
        val statusIcon = when (payout.status.lowercase()) {
            "completed" -> R.drawable.ic_status_completed
            "pending", "processing" -> R.drawable.ic_status_pending
            "payable" -> R.drawable.ic_status_payable
            "failed" -> R.drawable.ic_status_error
            else -> R.drawable.ic_status_pending
        }
        views.setImageViewResource(R.id.payout_status_icon, statusIcon)

        // Get Material You colors for text only
        val colors = WidgetColorHelper.getColors(context, isPayable)

        // Apply rounded card background drawable based on position
        val cardDrawable = if (isPayable) {
            R.drawable.widget_featured_card_background
        } else {
            // Determine position in non-payable list
            val nonPayablePayouts = payouts.filter { !it.status.equals("payable", ignoreCase = true) }
            val positionInList = nonPayablePayouts.indexOf(payout)
            val isFirst = positionInList == 0
            val isLast = positionInList == nonPayablePayouts.size - 1
            val isSingle = nonPayablePayouts.size == 1

            when {
                isSingle -> R.drawable.widget_card_background // All corners rounded
                isFirst -> R.drawable.widget_card_background_first // Top corners rounded
                isLast -> R.drawable.widget_card_background_last // Bottom corners rounded
                else -> R.drawable.widget_card_background_middle // Small corners all around
            }
        }
        views.setInt(R.id.payout_card, "setBackgroundResource", cardDrawable)

        // Apply text colors
        views.setTextColor(R.id.payout_amount, colors.primaryTextColor)
        views.setTextColor(R.id.payout_date, colors.secondaryTextColor)

        // Apply label color for featured items
        if (isPayable) {
            views.setTextColor(R.id.payout_label, colors.secondaryTextColor)
        }

        // Apply circular status icon background (with baked-in colors)
        val statusDrawable = if (isPayable) {
            R.drawable.widget_featured_status_background
        } else {
            R.drawable.widget_status_background
        }
        views.setInt(R.id.payout_status_container, "setBackgroundResource", statusDrawable)

        // Color the status icon
        views.setInt(R.id.payout_status_icon, "setColorFilter", colors.statusTextColor)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 2 // Regular and featured layouts

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
