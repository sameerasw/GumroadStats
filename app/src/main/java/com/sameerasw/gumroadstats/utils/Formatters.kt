package com.sameerasw.gumroadstats.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Formats a numeric string amount with thousand separators and two decimal places
 */
fun formatAmount(amount: String): String {
    return try {
        val number = amount.toDoubleOrNull() ?: return amount
        String.format(Locale.US, "%,.2f", number)
    } catch (e: Exception) {
        amount
    }
}

/**
 * Formats an ISO 8601 date string to a more readable format
 */
fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

