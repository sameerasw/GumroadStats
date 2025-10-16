package com.sameerasw.gumroadstats.ui.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A chip component that displays a payout status with an icon
 */
@Composable
fun StatusIconChip(status: String) {
    val (icon, color) = when (status.lowercase()) {
        "completed" -> Icons.Outlined.CheckCircle to MaterialTheme.colorScheme.primary
        "pending", "processing" -> Icons.Outlined.HourglassEmpty to MaterialTheme.colorScheme.tertiary
        "payable" -> Icons.Outlined.AttachMoney to MaterialTheme.colorScheme.secondary
        "failed" -> Icons.Outlined.Error to MaterialTheme.colorScheme.error
        else -> Icons.Outlined.HourglassEmpty to MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = color,
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp)
        )
    }
}

