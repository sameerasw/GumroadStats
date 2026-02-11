package com.sameerasw.gumroadstats.data.model

import com.google.gson.annotations.SerializedName

data class ProductsResponse(
    val success: Boolean,
    val products: List<Product>
)

data class ProductDetailResponse(
    val success: Boolean,
    val product: Product
)

data class ProductDeleteResponse(
    val success: Boolean,
    val message: String
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    @SerializedName("custom_summary") val customSummary: String?,
    @SerializedName("max_purchase_count") val maxPurchaseCount: Int?,
    @SerializedName("preview_url") val previewUrl: String?,
    @SerializedName("require_shipping") val requireShipping: Boolean,
    @SerializedName("subscription_duration") val subscriptionDuration: String?,
    val published: Boolean,
    val url: String,
    val price: Int,
    val currency: String,
    @SerializedName("short_url") val shortUrl: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    val tags: List<String>,
    @SerializedName("formatted_price") val formattedPrice: String,
    @SerializedName("sales_count") val salesCount: String?,
    @SerializedName("sales_usd_cents") val salesUsdCents: String?,
    @SerializedName("is_tiered_membership") val isTieredMembership: Boolean,
    val recurrences: List<String>?,
    val variants: List<ProductVariant>?,
    @SerializedName("purchasing_power_parity_prices") val pppPrices: Map<String, Int>?
)

data class ProductVariant(
    val title: String,
    val options: List<ProductOption>
)

data class ProductOption(
    val name: String,
    @SerializedName("price_difference") val priceDifference: Int,
    @SerializedName("is_pay_what_you_want") val isPayWhatYouWant: Boolean,
    @SerializedName("purchasing_power_parity_prices") val pppPrices: Map<String, Int>?,
    @SerializedName("recurrence_prices") val recurrencePrices: Map<String, RecurrencePrice>?
)

data class RecurrencePrice(
    @SerializedName("price_cents") val priceCents: Int,
    @SerializedName("suggested_price_cents") val suggestedPriceCents: Int?,
    @SerializedName("purchasing_power_parity_prices") val pppPrices: Map<String, Int>?
)
