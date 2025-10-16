package com.sameerasw.gumroadstats.widget

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Helper class to extract Material You colors for the widget
 */
object WidgetColorHelper {

    data class WidgetColors(
        val backgroundColor: Int,
        val primaryTextColor: Int,
        val secondaryTextColor: Int,
        val statusTextColor: Int,
        val statusBackgroundColor: Int
    )

    fun getColors(context: Context, isFeatured: Boolean = false): WidgetColors {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ with Material You dynamic colors
            getMaterialYouColors(context, isFeatured)
        } else {
            // Fallback colors for older Android versions
            getFallbackColors(context, isFeatured)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getMaterialYouColors(context: Context, isFeatured: Boolean): WidgetColors {
        val isDarkMode = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        return if (isFeatured) {
            // Featured (payable) payout colors - primary container
            if (isDarkMode) {
                WidgetColors(
                    backgroundColor = context.getColor(android.R.color.system_accent1_700),
                    primaryTextColor = context.getColor(android.R.color.system_accent1_100),
                    secondaryTextColor = context.getColor(android.R.color.system_accent1_200),
                    statusTextColor = context.getColor(android.R.color.system_accent2_200),
                    statusBackgroundColor = context.getColor(android.R.color.system_accent2_800)
                )
            } else {
                WidgetColors(
                    backgroundColor = context.getColor(android.R.color.system_accent1_100),
                    primaryTextColor = context.getColor(android.R.color.system_accent1_900),
                    secondaryTextColor = context.getColor(android.R.color.system_accent1_700),
                    statusTextColor = context.getColor(android.R.color.system_accent2_700),
                    statusBackgroundColor = context.getColor(android.R.color.system_accent2_100)
                )
            }
        } else {
            // Regular payout colors - surface container
            if (isDarkMode) {
                WidgetColors(
                    backgroundColor = context.getColor(android.R.color.system_neutral1_800),
                    primaryTextColor = context.getColor(android.R.color.system_accent1_200),
                    secondaryTextColor = context.getColor(android.R.color.system_neutral1_300),
                    statusTextColor = context.getColor(android.R.color.system_accent3_200),
                    statusBackgroundColor = context.getColor(android.R.color.system_accent3_800)
                )
            } else {
                WidgetColors(
                    backgroundColor = context.getColor(android.R.color.system_neutral1_50),
                    primaryTextColor = context.getColor(android.R.color.system_accent1_600),
                    secondaryTextColor = context.getColor(android.R.color.system_neutral1_700),
                    statusTextColor = context.getColor(android.R.color.system_accent3_600),
                    statusBackgroundColor = context.getColor(android.R.color.system_accent3_100)
                )
            }
        }
    }

    private fun getFallbackColors(context: Context, isFeatured: Boolean): WidgetColors {
        val isDarkMode = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        return if (isFeatured) {
            if (isDarkMode) {
                WidgetColors(
                    backgroundColor = 0xFF3C2F52.toInt(), // Dark purple
                    primaryTextColor = 0xFFE8DEF8.toInt(), // Light purple
                    secondaryTextColor = 0xFFCCC2DC.toInt(), // Medium purple
                    statusTextColor = 0xFFEFB8C8.toInt(), // Light pink
                    statusBackgroundColor = 0xFF633B48.toInt() // Dark pink
                )
            } else {
                WidgetColors(
                    backgroundColor = 0xFFE8DEF8.toInt(), // Light purple
                    primaryTextColor = 0xFF1D192B.toInt(), // Dark purple
                    secondaryTextColor = 0xFF49454F.toInt(), // Medium purple
                    statusTextColor = 0xFF7D5260.toInt(), // Accent purple
                    statusBackgroundColor = 0xFFFFD8E4.toInt() // Light pink bg
                )
            }
        } else {
            if (isDarkMode) {
                WidgetColors(
                    backgroundColor = 0xFF1C1B1F.toInt(), // Dark surface
                    primaryTextColor = 0xFFD0BCFF.toInt(), // Primary light
                    secondaryTextColor = 0xFFCAC4D0.toInt(), // On surface variant
                    statusTextColor = 0xFFEFB8C8.toInt(), // Tertiary light
                    statusBackgroundColor = 0xFF4A4458.toInt() // Tertiary container dark
                )
            } else {
                WidgetColors(
                    backgroundColor = 0xFFFEF7FF.toInt(), // Light surface
                    primaryTextColor = 0xFF6750A4.toInt(), // Primary
                    secondaryTextColor = 0xFF49454F.toInt(), // On surface variant
                    statusTextColor = 0xFF7D5260.toInt(), // Tertiary
                    statusBackgroundColor = 0xFFFFD8E4.toInt() // Tertiary container
                )
            }
        }
    }
}
