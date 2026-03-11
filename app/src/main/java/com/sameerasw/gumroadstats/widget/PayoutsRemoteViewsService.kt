package com.sameerasw.gumroadstats.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.graphics.drawable.GradientDrawable
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.local.PayoutsCache
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.utils.formatAmount
import com.sameerasw.gumroadstats.utils.formatDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

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
    private val cache = PayoutsCache.getInstance(context)

    override fun onCreate() {
        loadPayouts()
    }

    override fun onDataSetChanged() {
        loadPayouts()
    }

    private fun loadPayouts() {
        try {
            runBlocking {
                val cachedPayouts = withTimeoutOrNull(2000) {
                    cache.cachedPayouts.first()
                }
                
                if (cachedPayouts != null && cachedPayouts.isNotEmpty()) {
                    Log.d("PayoutsWidget", "Loaded ${cachedPayouts.size} payouts from DataStore")
                    payouts = sortPayouts(cachedPayouts)
                } else {
                    Log.d("PayoutsWidget", "DataStore empty or timed out, trying SharedPreferences fallback")
                    val fallbackJson = context.getSharedPreferences("widget_payouts_sync", Context.MODE_PRIVATE)
                        .getString("payouts_json", null)
                    
                    if (fallbackJson != null) {
                        try {
                            val gson = Gson()
                            val payoutsArray = gson.fromJson(fallbackJson, Array<Payout>::class.java)
                            val fallbackPayouts = payoutsArray.toList()
                            if (fallbackPayouts.isNotEmpty()) {
                                Log.d("PayoutsWidget", "Loaded ${fallbackPayouts.size} payouts from fallback")
                                payouts = sortPayouts(fallbackPayouts)
                            } else {
                                payouts = emptyList()
                            }
                        } catch (e: Exception) {
                            Log.e("PayoutsWidget", "Error deserializing fallback JSON", e)
                            payouts = emptyList()
                        }
                    } else {
                        Log.w("PayoutsWidget", "No data in fallback storage either")
                        payouts = emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PayoutsWidget", "Error loading payouts", e)
            payouts = emptyList()
        }
    }

    private fun sortPayouts(list: List<Payout>): List<Payout> {
        val payablePayout = list.firstOrNull {
            it.status?.equals("payable", ignoreCase = true) == true
        }
        val otherPayouts = list.filter {
            it.status?.equals("payable", ignoreCase = true) != true
        }

        return if (payablePayout != null) {
            listOf(payablePayout) + otherPayouts
        } else {
            list
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
        views.setTextViewText(R.id.payout_amount, "${formatAmount(payout.amount)} ${payout.currency.uppercase()}")
        views.setTextViewText(R.id.payout_date, formatDate(payout.createdAt))

        // Set status icon based on status type
        val status = payout.status
        val statusIcon = when (status.lowercase()) {
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

        // Set fill-in intent for clicking on this item
        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.payout_card, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 2 // Regular and featured layouts

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
