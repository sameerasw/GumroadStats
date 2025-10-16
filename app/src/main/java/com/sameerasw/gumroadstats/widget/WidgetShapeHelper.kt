package com.sameerasw.gumroadstats.widget

import android.graphics.drawable.GradientDrawable

/**
 * Helper to create rounded drawable shapes for widget elements
 */
object WidgetShapeHelper {

    /**
     * Creates a rounded rectangle drawable with the specified color and corner radius
     */
    fun createRoundedDrawable(color: Int, cornerRadiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = cornerRadiusDp
        }
    }

    /**
     * Creates a circle drawable (for icon containers)
     */
    fun createCircleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }
}

