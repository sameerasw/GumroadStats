package com.sameerasw.gumroadstats.data.model

import com.google.gson.annotations.SerializedName

data class PayoutsResponse(
    val success: Boolean,
    val payouts: List<Payout>,
    @SerializedName("next_page_url")
    val nextPageUrl: String?,
    @SerializedName("next_page_key")
    val nextPageKey: String?
)

data class Payout(
    val id: String?,
    val amount: String,
    val currency: String,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("processed_at")
    val processedAt: String?,
    @SerializedName("payment_processor")
    val paymentProcessor: String,
    @SerializedName("bank_account_visual")
    val bankAccountVisual: String?,
    @SerializedName("paypal_email")
    val paypalEmail: String?
)
