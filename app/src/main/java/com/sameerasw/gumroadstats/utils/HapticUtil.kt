package com.sameerasw.gumroadstats.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticUtil {
    fun performClick(haptic: HapticFeedback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
             haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performLightTick(haptic: HapticFeedback) {
        // Compose doesn't expose SEGMENT_TICK or CLOCK_TICK directly easily without View
        // We can approximate or just use TextHandleMove which is subtle
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
