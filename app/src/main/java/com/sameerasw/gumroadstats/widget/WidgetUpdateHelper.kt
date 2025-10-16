package com.sameerasw.gumroadstats.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.sameerasw.gumroadstats.R

/**
 * Helper object to update all widgets when data changes
 */
object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, PayoutsWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        if (appWidgetIds.isNotEmpty()) {
            // Update all widgets
            for (appWidgetId in appWidgetIds) {
                PayoutsWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId)
            }

            // Notify that data has changed so ListView refreshes
            for (appWidgetId in appWidgetIds) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_payouts_list)
            }
        }
    }
}
