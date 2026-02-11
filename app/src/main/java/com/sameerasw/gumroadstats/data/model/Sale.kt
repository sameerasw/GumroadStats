package com.sameerasw.gumroadstats.data.model

import com.google.gson.annotations.SerializedName

data class SalesResponse(
    val success: Boolean,
    val sales: List<Sale>,
    @SerializedName("next_page_url")
    val nextPageUrl: String?,
    @SerializedName("next_page_key")
    val nextPageKey: String?
)

data class SaleDetailsResponse(
    val success: Boolean,
    val sale: Sale
)

data class Sale(
    val id: String,
    val email: String,
    @SerializedName("seller_id")
    val sellerId: String,
    val timestamp: String,
    val daystamp: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_has_variants")
    val productHasVariants: Boolean,
    val price: Long, // in cents
    @SerializedName("gumroad_fee")
    val gumroadFee: Long, // in cents
    @SerializedName("formatted_display_price")
    val formattedDisplayPrice: String,
    @SerializedName("formatted_total_price")
    val formattedTotalPrice: String,
    @SerializedName("currency_symbol")
    val currencySymbol: String,
    @SerializedName("amount_refundable_in_currency")
    val amountRefundableInCurrency: String,
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_permalink")
    val productPermalink: String,
    @SerializedName("partially_refunded")
    val partiallyRefunded: Boolean,
    val chargedback: Boolean,
    @SerializedName("purchase_email")
    val purchaseEmail: String,
    val paid: Boolean,
    @SerializedName("has_variants")
    val hasVariants: Boolean,
    @SerializedName("variants_and_quantity")
    val variantsAndQuantity: String?,
    @SerializedName("order_id")
    val orderId: Long,
    @SerializedName("is_product_physical")
    val isProductPhysical: Boolean,
    @SerializedName("purchaser_id")
    val purchaserId: String,
    @SerializedName("is_recurring_billing")
    val isRecurringBilling: Boolean,
    @SerializedName("can_contact")
    val canContact: Boolean,
    @SerializedName("referrer")
    val referrer: String?,
    val quantity: Int,
    val refunded: Boolean = false,
    val shipped: Boolean = false
)
