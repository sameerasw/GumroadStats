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
fun formatDate(dateString: String, is24Hour: Boolean = true): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return dateString
        
        val pattern = if (is24Hour) {
            "MMM dd, yyyy HH:mm"
        } else {
            "MMM dd, yyyy hh:mm a"
        }
        val outputFormat = SimpleDateFormat(pattern, Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

