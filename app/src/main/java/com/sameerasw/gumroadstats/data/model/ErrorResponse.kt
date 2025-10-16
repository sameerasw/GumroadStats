package com.sameerasw.gumroadstats.data.model

/**
 * Standard error response from Gumroad API
 */
data class ErrorResponse(
    val success: Boolean,
    val message: String?
)
