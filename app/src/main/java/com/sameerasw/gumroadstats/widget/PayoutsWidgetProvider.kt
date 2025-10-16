package com.sameerasw.gumroadstats.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sameerasw.gumroadstats.R

/**
 * Widget provider for displaying Gumroad payouts with Material You colors
 */
class PayoutsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Listen for configuration changes (theme changes)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, PayoutsWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_payouts)

            // Apply theme colors to empty view
            val colors = WidgetColorHelper.getColors(context, isFeatured = false)
            views.setTextColor(R.id.widget_empty_view, colors.secondaryTextColor)

            // Set up the intent that starts the PayoutsRemoteViewsService
            val intent = Intent(context, PayoutsRemoteViewsService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            // Set the adapter for the list view
            views.setRemoteAdapter(R.id.widget_payouts_list, intent)

            // Set empty view
            views.setEmptyView(R.id.widget_payouts_list, R.id.widget_empty_view)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_payouts_list)
        }
    }
}
